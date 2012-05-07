package com.meltmedia.cadmium.jgroups.receivers;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jgroups.Address;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.jgroups.ContentService;
import com.meltmedia.cadmium.jgroups.ContentServiceListener;
import com.meltmedia.cadmium.jgroups.CoordinatedWorker;
import com.meltmedia.cadmium.jgroups.CoordinatedWorkerListener;
import com.meltmedia.cadmium.jgroups.SiteDownService;

@Singleton
public class UpdateChannelReceiver extends ExtendedReceiverAdapter implements CoordinatedWorkerListener, ContentServiceListener {
  
  public static final String BASE_PATH = "ApplicationBasePath";
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  public static enum ProtocolMessage {
    UPDATE, READY, UPDATE_DONE, UPDATE_FAILED, CURRENT_STATE
  }
  
  public static enum UpdateState {
    IDLE, UPDATING, WAITING
  }
  
  protected JChannel channel;
  
  protected CoordinatedWorker worker;
  
  protected ContentService content;
  
  protected SiteDownService sd;
  
  protected String parentPath;
  
  private Map<String, UpdateState> currentStates = new Hashtable<String, UpdateState>();
  private UpdateState myState = UpdateState.IDLE;
  private String newDir;
  
  @Inject
  public UpdateChannelReceiver(JChannel channel, CoordinatedWorker worker, ContentService content, SiteDownService sd, @Named(BASE_PATH) String parentPath) {
    this.channel = channel;
    this.channel.setReceiver(this);
    this.worker = worker;
    this.content = content;
    this.content.setListener(this);
    this.sd = sd;
    this.worker.setListener(this);
    this.parentPath = parentPath;
    viewAccepted(channel.getView());
  }


  @Override
  public void receive(Message msg) {
    String message = msg.getObject().toString();
    log.debug("Received a message state {}, message {}, src {}",new Object[] { myState, message, msg.getSrc()});
    if(message.equals(ProtocolMessage.CURRENT_STATE.name())) {
      log.info("Responding with current state {}", myState);
      Message reply = msg.makeReply();
      reply.setObject(myState.name());
      try {
        channel.send(reply);
      } catch (ChannelNotConnectedException e) {
        log.error("The channel is not connected", e);
      } catch (ChannelClosedException e) {
        log.error("The channel is closed", e);
      }
    } else if (message.replaceAll("\\A(\\w+)\\s.*\\Z", "$1").equals(ProtocolMessage.UPDATE.name())) {
      if(myState == UpdateState.IDLE) {
        log.info("Beginning an update, started by {}", msg.getSrc());
        myState = UpdateState.UPDATING;
        
        try {
          channel.send(new Message(null, null, myState.name()));
        } catch (Exception e) {
          log.error("Failed to send message to update peir's view of my state", e);
        }
        
        // Parse out message parameters
        
        Map<String, String> workProperties = new HashMap<String, String>();
        if(this.parentPath != null && this.parentPath.length() > 0) {
          workProperties.put("basePath", this.parentPath);
        }
        message = message.substring(ProtocolMessage.UPDATE.name().length()).trim();
        if(message.length() > 0) {
          String msgParams[] = message.split(";");
          for(String msgParam : msgParams) {
            if(msgParam.indexOf("=") > -1) {
              workProperties.put(msgParam.substring(0, msgParam.indexOf("=")).trim(), msgParam.substring(msgParam.indexOf("=") + 1).trim());            
            }
          }
        }
        
        // Begin work 
        worker.beginPullUpdates(workProperties);
      } else {
        log.info("Received "+message+" message with current state [{}] not IDLE from {}", myState, msg.getSrc());
      }
    } else if (message.equals(ProtocolMessage.UPDATE_DONE.name())) {
      log.info("Update is done @ {}", msg.getSrc());
      try {
        channel.send(new Message(null, null, myState.name()));
      } catch (Exception e) {
        log.error("Failed to send message to update peir's view of my state", e);
      }
    } else if (message.equals(ProtocolMessage.UPDATE_FAILED.name())) {
      if(myState != UpdateState.IDLE) {
        log.info("update has failed @ {}", msg.getSrc());
        worker.killUpdate();
        myState = UpdateState.IDLE;
        try {
          channel.send(new Message(null, null, myState.name()));
        } catch (Exception e) {
          log.error("Failed to send message to update peir's view of my state", e);
        }
      }
    } else {
      log.debug("I might have received a state update");
      String messageName = message.replaceAll("\\A(\\w+)\\s.*\\Z", "$1");
      try{
        UpdateState state = UpdateState.valueOf(messageName);
        if(currentStates.containsKey(msg.getSrc().toString())) {
          currentStates.put(msg.getSrc().toString(), state);
          log.info("Updating state of {} to {}", msg.getSrc(), state);
        }
      } catch(Exception e) {
        log.warn("Invalid message received \"{}\", parsed \"{}\", error msg \"{}\"", new Object[] {message, messageName, e.getMessage()});
      }
      
      if(myState == UpdateState.WAITING) {
        boolean updateDone = true;
        for(String peir : currentStates.keySet()) {
          if(currentStates.get(peir) != UpdateState.WAITING) {
            updateDone = false;
            break;
          }
        }
        if(updateDone) {
          log.info("Done updating content now switching content.");
          sd.start();
          content.switchContent(newDir);
          myState = UpdateState.IDLE;
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void viewAccepted(View new_view) {
    Vector<Address> members = new_view.getMembers();
    
    for(Address member : members) {
      if(!currentStates.containsKey(member.toString())) {
        log.info("Discovered new member {}", member.toString());
        currentStates.put(member.toString(), UpdateState.IDLE);
        try {
          channel.send(new Message(member, null, ProtocolMessage.CURRENT_STATE.name()));
        } catch (Exception e) {
          log.error("Failed to send message to check for peir's current state", e);
        }
      }
    }
  }


  @Override
  public void workDone(String newDir) {
    log.info("Work is done");
    this.newDir = newDir;
    myState = UpdateState.WAITING;
    try {
      channel.send(new Message(null, null, ProtocolMessage.UPDATE_DONE.name()));
    } catch (Exception e) {
      log.error("Failed to send message to update peir's current state of me to done", e);
    }
  }


  @Override
  public void workFailed() {
    log.info("Work has failed");
    try {
      channel.send(new Message(null, null, ProtocolMessage.UPDATE_FAILED.name()));
    } catch (Exception e) {
      log.error("Failed to send message to update peir's current state of me to failed", e);
    }
  }


  Map<String, UpdateState> getCurrentStates() {
    return currentStates;
  }


  void setCurrentStates(Map<String, UpdateState> currentStates) {
    this.currentStates = currentStates;
  }


  public UpdateState getMyState() {
    return myState;
  }


  void setMyState(UpdateState myState) {
    this.myState = myState;
  }


  @Override
  public void doneSwitching() {
    sd.stop();
    log.info("Done switching content.");
  }
  
}
