package com.meltmedia.cadmium.core.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
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
  
  @Inject
  protected MessageSender sender;
  
  @Inject
  protected JChannel channel;
  
  @Inject
  protected List<ChannelMember> members;
  
  @Inject
  protected Properties configProperties;

  @SuppressWarnings("unchecked")
  @Override
  public void viewAccepted(View new_view) {
    if(this.members != null) {
      List<Address> memberAddresses = new_view.getMembers();
      
      addNewMembers(memberAddresses);
      
      pergeDroppedMembers(memberAddresses);
      
      handleSyncRequest(new_view);
    }
  }

  private void handleSyncRequest(View new_view) {
    log.debug("Here is the new view {}", new_view);
    ChannelMember coordinator = getCoordinator();
    if(coordinator != null && !coordinator.isMine()) {
      log.debug("I'm not the coordinator!!!");
      Message syncMessage = new Message();
      syncMessage.setCommand(ProtocolMessage.SYNC);
      log.info("Sending sync message to coordinator {}", coordinator.getAddress());
      if(configProperties.containsKey("branch") && configProperties.containsKey("git.ref.sha")) {
        syncMessage.getProtocolParameters().put("branch", configProperties.getProperty("branch"));
        syncMessage.getProtocolParameters().put("sha", configProperties.getProperty("git.ref.sha"));
      }
      try{
        sender.sendMessage(syncMessage, coordinator);
      } catch(Exception e) {
        log.warn("Failed to send sync message: {}", e.getMessage());
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
      }
    }
  }

  private void addNewMembers(List<Address> memberAddresses) {
    for(Address member : memberAddresses) {
      ChannelMember cMember = new ChannelMember(member, isCoordinator(member), isMine(member));
      if(!members.contains(cMember)) {
        log.info("Discovered new member {}, coordinator {}, me {}", new Object[] {member.toString(), cMember.isCoordinator(), cMember.isMine()});
        members.add(cMember);
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
  
  private boolean isCoordinator(Address newAddress) {
    boolean coord = false;
    if(newAddress != null && newAddress.toString().equals(channel.getView().getCreator().toString())) {
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
