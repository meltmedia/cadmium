package com.meltmedia.cadmium.core.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.MembershipListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MembershipTracker extends MembershipListenerAdapter {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  protected MessageSender sender;
  
  protected JChannel channel;
  
  protected List<ChannelMember> members;
  
  protected Properties configProperties;
  
  @Inject
  public MembershipTracker(MessageSender sender, JChannel channel, @Named("members") List<ChannelMember> members, @Named("config.properties") Properties configProperties) {
    this.sender = sender;
    this.channel = channel;
    this.members = members;
    this.configProperties = configProperties;
    if(this.channel != null) {
      viewAccepted(this.channel.getView());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void viewAccepted(View new_view) {
    if(this.members != null) {
      log.info("Received a new view ["+new_view.size()+"]");
      List<Address> memberAddresses = new_view.getMembers();
      
      List<ChannelMember> newMembers = addNewMembers(memberAddresses, new_view);
      
      pergeDroppedMembers(memberAddresses);
      
      fixCoordinator(new_view);
      
      sendStateMessages(newMembers);
      
      handleSyncRequest(new_view);
      log.info("Processed new view now there are ["+new_view.size()+"] members");
    } else {
      log.warn("Received a new view members list is null");
    }
  }

  private void fixCoordinator(View newView) {
    if(members != null) {
      for(ChannelMember member : members) {
        if(isCoordinator(member.getAddress(), newView)) {
          member.setCoordinator(true);
          log.info("Coordinator is ["+member.getAddress()+"]");
        } else {
          member.setCoordinator(false);
          log.info("Member ["+member.getAddress()+"] is not coordinator");
        }
      }
    }
  }

  private void handleSyncRequest(View new_view) {
    log.info("Here is the new view {}", new_view);
    ChannelMember coordinator = getCoordinator();
    if(coordinator != null && !coordinator.isMine()) {
      log.debug("I'm not the coordinator!!!");
      Message syncMessage = new Message();
      syncMessage.setCommand(ProtocolMessage.SYNC);
      log.info("Sending sync message to coordinator {}", coordinator.getAddress());
      if(configProperties.containsKey("branch") && configProperties.containsKey("git.ref.sha")) {
        syncMessage.getProtocolParameters().put("branch", configProperties.getProperty("branch"));
        syncMessage.getProtocolParameters().put("sha", configProperties.getProperty("git.ref.sha"));
        log.info("I have branch:{}, and sha:{}", configProperties.getProperty("branch"), configProperties.getProperty("git.ref.sha"));
      }
      try{
        sender.sendMessage(syncMessage, coordinator);
      } catch(Exception e) {
        log.warn("Failed to send sync message: {}", e.getMessage());
      }
    }
  }
  
  private void sendStateMessages(List<ChannelMember> newMembers) {
    if(newMembers != null) {
      for(ChannelMember cMember : newMembers) {
        Message stateMsg = new Message();
        stateMsg.setCommand(ProtocolMessage.CURRENT_STATE);
        try {
          sender.sendMessage(stateMsg, cMember);
        } catch (Exception e) {
          log.error("Failed to send message to check for peir's current state", e);
        }
      }
    }
  }

  private void pergeDroppedMembers(List<Address> memberAddresses) {
    List<ChannelMember> oldMembers = new ArrayList<ChannelMember>();
    oldMembers.addAll(members);
    for(ChannelMember member : oldMembers) {
      boolean found = false;
      for(Address newMember : memberAddresses) {
        if(member.getAddress().toString().equals(newMember.toString())) {
          found = true;
        }
      }
      if(!found) {
        members.remove(member);
        log.info("Purging old member {}, was coordinator {}, me {}", new Object[] {member.getAddress().toString(), member.isCoordinator(), member.isMine()});
      }
    }
  }

  private List<ChannelMember> addNewMembers(List<Address> memberAddresses, View newView) {
    List<ChannelMember> newMembers = new ArrayList<ChannelMember>();
    for(Address member : memberAddresses) {
      ChannelMember cMember = new ChannelMember(member, isCoordinator(member, newView), isMine(member));
      if(!members.contains(cMember)) {
        log.info("Discovered new member {}, coordinator {}, me {}", new Object[] {member.toString(), cMember.isCoordinator(), cMember.isMine()});
        members.add(cMember);
        newMembers.add(cMember);
      }
    }
    return newMembers;
  }
  
  private boolean isCoordinator(Address newAddress, View newView) {
    boolean coord = false;
    if(newAddress != null && newAddress.toString().equals(newView.getCreator().toString())) {
      coord = true;
    }
    return coord;
  }
  
  private boolean isMine(Address newAddress) {
    boolean mine = false;
    if(newAddress != null && newAddress.toString().equals(channel.getLocalAddress().toString())) {
      mine = true;
    }
    return mine;
  }
  
  public ChannelMember getCoordinator() {
    if(members != null) {
      for(ChannelMember member : members) {
        if(member.isCoordinator()) {
          return member;
        }
      }
    }
    return null;
  }

}
