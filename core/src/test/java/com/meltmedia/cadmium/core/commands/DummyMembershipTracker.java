package com.meltmedia.cadmium.core.commands;

import java.util.List;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;

public class DummyMembershipTracker extends MembershipTracker {
  public DummyMembershipTracker(){
    super(null,null,null,null);
  }
  
  public void setMembers(List<ChannelMember> members) {
    this.members = members;
  }
  
  public List<ChannelMember> getMembers() {
    return this.members;
  }
}
