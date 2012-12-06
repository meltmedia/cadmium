package com.meltmedia.cadmium.core.messaging;

import org.jgroups.Address;

public interface MembershipTracker {
  
  public ChannelMember getCoordinator();
  public void updateMembersIp(Address aMember, String ip);
  public String getMembersIp(Address aMember);
  public void sendMyIp(ChannelMember member) throws Exception;

}
