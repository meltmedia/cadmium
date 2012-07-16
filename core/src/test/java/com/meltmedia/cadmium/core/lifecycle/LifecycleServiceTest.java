package com.meltmedia.cadmium.core.lifecycle;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.core.messaging.jgroups.DummyJChannel;
import com.meltmedia.cadmium.core.messaging.jgroups.JGroupsMessageSender;

public class LifecycleServiceTest {

  @Test
  public void testUpdateState() throws Exception {
    Address me = new IpAddress(1234);
    Address other2 = new IpAddress(4322);
    Address other3 = new IpAddress(4321);
    Vector<Address> viewMems = new Vector<Address>();
    viewMems.add(other2);
    viewMems.add(me);
    viewMems.add(other3);
    
    DummyJChannel channel = new DummyJChannel(me, viewMems);
    JGroupsMessageSender sender = new JGroupsMessageSender();
    sender.setChannel(channel);
    
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, false, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4322), false, true, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.IDLE));
    
    LifecycleService service = new LifecycleService();
    service.members = members;
    service.sender = sender;
    
    service.updateState(new ChannelMember(new IpAddress(4322)), UpdateState.UPDATING);
    
    assertTrue("State not updated", members.get(1).getState() == UpdateState.UPDATING);
    assertTrue("No message sent", channel.getMessageList().size() == 0);
  }

  @Test
  public void testUpdateMyState() throws Exception {
    Address me = new IpAddress(1234);
    Address other2 = new IpAddress(4322);
    Address other3 = new IpAddress(4321);
    Vector<Address> viewMems = new Vector<Address>();
    viewMems.add(other2);
    viewMems.add(me);
    viewMems.add(other3);
    
    DummyJChannel channel = new DummyJChannel(me, viewMems);
    JGroupsMessageSender sender = new JGroupsMessageSender();
    sender.setChannel(channel);
    
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, false, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4322), false, true, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.IDLE));
    
    LifecycleService service = new LifecycleService();
    service.members = members;
    service.sender = sender;
    
    service.updateMyState(UpdateState.UPDATING);
    
    assertTrue("State not updated", members.get(1).getState() == UpdateState.UPDATING);
    assertTrue("No message sent", channel.getMessageList().size() == 1);
    assertTrue("Message sent not a STATE_UPDATE", channel.getMessageList().get(0).getObject().toString().contains(ProtocolMessage.STATE_UPDATE));
    assertTrue("Correct state not sent", channel.getMessageList().get(0).getObject().toString().contains(UpdateState.UPDATING.name()));
  }
  
  @Test
  public void testGetCurrentState() throws Exception {
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, false, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4322), false, true, UpdateState.WAITING));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.IDLE));
    
    LifecycleService service = new LifecycleService();
    service.members = members;
    
    assertTrue("Wrong State", service.getCurrentState() == UpdateState.WAITING);
  }
  
  @Test
  public void testAllEquals() throws Exception {
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, false, UpdateState.WAITING));
    members.add(new ChannelMember(new IpAddress(4322), false, true, UpdateState.WAITING));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.WAITING));
    
    LifecycleService service = new LifecycleService();
    service.members = members;
    
    assertTrue("All should equal", service.allEquals(UpdateState.WAITING));
    assertTrue("All should not equal", !service.allEquals(UpdateState.UPDATING));
    
    members.get(1).setState(UpdateState.UPDATING);

    assertTrue("All should definitely not equal", !service.allEquals(UpdateState.WAITING));
    
  }
}
