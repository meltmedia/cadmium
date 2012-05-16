package com.meltmedia.cadmium.core.lifecycle;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class LifecycleService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  @Named("members")
  List<ChannelMember> members;
  
  @Inject
  MessageSender sender;
  
  public void updateState(ChannelMember member, UpdateState state) {
    if(members != null && members.contains(member)) {
      log.info("Updating state of {} to {}", member.getAddress().toString(), state);
      member = members.get(members.indexOf(member));
      member.setState(state);
    }
  }
  
  public boolean isMe(ChannelMember member) {
    if(members != null && members.contains(member)) {
      return members.get(members.indexOf(member)).isMine();
    }
    return false;
  }
  
  public void updateMyState(UpdateState state) {
    updateMyState(state, true);
  }
  
  public void updateMyState(UpdateState state, boolean sendUpdate) {
    if(members != null) {
      for(ChannelMember member : members) {
        if(member.isMine()) {
          UpdateState oldState = member.getState();
          member.setState(state);
          if(oldState != state) {
            log.info("Updating my state to {} and sendUpdate is {}", state, sendUpdate);
            if(sendUpdate) {
              sendStateUpdate(null);
            }
          }
        }
      }
    }
  }
  
  public void sendStateUpdate(ChannelMember source) {
    Message updateStateMsg = new Message();
    updateStateMsg.setCommand(ProtocolMessage.STATE_UPDATE);
    updateStateMsg.getProtocolParameters().put("state", getCurrentState().name());
    try{
      log.info("Sending state update message from state change!");
      sender.sendMessage(updateStateMsg, source);
    } catch(Exception e) {
      log.warn("Failed to send state update: {}", e.getMessage());
    }
  }
  
  public UpdateState getCurrentState() {
    if(members != null) {
      for(ChannelMember member : members) {
        if(member.isMine()) {
          return member.getState();
        }
      }
    }
    return null;
  }
  
  public UpdateState getState(ChannelMember member) {
    if(members != null) {
      if(members.contains(member)) {
        return members.get(members.indexOf(member)).getState();
      }
    }
    return null;
  }
  
  public boolean allEquals(UpdateState state) {
    if(members != null) {
      log.info("Checking if all {} members are in {} state", members.size(), state);
      boolean allEqual = true;
      for(ChannelMember member : members) {
        if(member.getState() != state) {
          allEqual = false;
          break;
        }
      }
      return allEqual;
    }
    return false;
  }

  public void setMembers(List<ChannelMember> members) {
    this.members = members;
  }

  public void setSender(MessageSender sender) {
    this.sender = sender;
  }

}
