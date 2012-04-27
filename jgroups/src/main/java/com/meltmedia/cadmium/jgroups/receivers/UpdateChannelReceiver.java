package com.meltmedia.cadmium.jgroups.receivers;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

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
import com.meltmedia.cadmium.jgroups.CoordinatedWorker;
import com.meltmedia.cadmium.jgroups.CoordinatedWorkerListener;

public class UpdateChannelReceiver extends ExtendedReceiverAdapter implements CoordinatedWorkerListener {
  private Logger log = LoggerFactory.getLogger(getClass());
  
  public static enum ProtocolMessage {
    UPDATE, READY, UPDATE_DONE, UPDATE_FAILED, CURRENT_STATE
  }
  
  public static enum UpdateState {
    IDLE, UPDATING, WAITING
  }
  
  private JChannel channel;
  private CoordinatedWorker worker;
  private ContentService content;
  private Map<String, UpdateState> currentStates = new Hashtable<String, UpdateState>();
  private UpdateState myState = UpdateState.IDLE;
  private File newDir;
  
  public UpdateChannelReceiver(JChannel channel, CoordinatedWorker worker, ContentService content) {
    this.channel = channel;
    this.channel.setReceiver(this);
    this.worker = worker;
    this.content = content;
    this.worker.setListener(this);
    viewAccepted(channel.getView());
  }


  @Override
  public void receive(Message msg) {
    String message = msg.getObject().toString();
    log.debug("Received a message state {}, message {}, src {}",new Object[] { myState, message, msg.getSrc()});
    if(message.equals(ProtocolMessage.CURRENT_STATE.name())) {
      log.debug("Responding with current state");
      Message reply = msg.makeReply();
      reply.setObject(myState.name());
      try {
        channel.send(reply);
      } catch (ChannelNotConnectedException e) {
        log.error("The channel is not connected", e);
      } catch (ChannelClosedException e) {
        log.error("The channel is closed", e);
      }
    } else if (myState == UpdateState.IDLE && message.startsWith(ProtocolMessage.UPDATE.name())) {
      log.debug("Beginning an update");
      myState = UpdateState.UPDATING;
      
      try {
        channel.send(new Message(null, null, myState.name()));
      } catch (Exception e) {
        log.error("Failed to send message to update peir's view of my state", e);
      }
      
      // Parse out message parameters
      
      Map<String, String> workProperties = new HashMap<String, String>();
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
    } else if (message.equals(ProtocolMessage.UPDATE_DONE.name())) {
      log.debug("Update is done");
      try {
        channel.send(new Message(null, null, myState.name()));
      } catch (Exception e) {
        log.error("Failed to send message to update peir's view of my state", e);
      }
    } else if (myState != UpdateState.IDLE && message.equals(ProtocolMessage.UPDATE_FAILED.name())) {
      log.debug("update has failed");
      worker.killUpdate();
      myState = UpdateState.IDLE;
      try {
        channel.send(new Message(null, null, myState.name()));
      } catch (Exception e) {
        log.error("Failed to send message to update peir's view of my state", e);
      }
    } else {
      log.debug("I might have received a state update");
      try{
        UpdateState state = UpdateState.valueOf(message);
        if(currentStates.containsKey(msg.getSrc().toString())) {
          currentStates.put(msg.getSrc().toString(), state);
          log.info("Updating state of {} to {}", msg.getSrc(), state);
        }
      } catch(Exception e) {
        log.warn("Invalid message received {}, {}", message, e.getMessage());
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
  public void workDone(File newDir) {
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
    //myState = UpdateState.IDLE;
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
  
}
