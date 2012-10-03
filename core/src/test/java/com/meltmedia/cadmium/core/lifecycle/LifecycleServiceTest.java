/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.lifecycle;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MessageConverter;
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
    members.add(new ChannelMember(new IpAddress(1234), true, false, UpdateState.IDLE, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4322), false, true, UpdateState.IDLE, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.IDLE, UpdateState.IDLE));
    
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
    sender.setConverter(new MessageConverter());
    
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, false, UpdateState.IDLE, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4322), false, true, UpdateState.IDLE, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.IDLE, UpdateState.IDLE));
    
    LifecycleService service = new LifecycleService();
    service.members = members;
    service.sender = sender;
    
    service.updateMyState(UpdateState.UPDATING);
    
    assertTrue("State not updated", members.get(1).getState() == UpdateState.UPDATING);
    assertTrue("No message sent", channel.getMessageList().size() == 1);
    assertTrue("Message sent not a STATE_UPDATE", new String(channel.getMessageList().get(0).getBuffer(), "UTF-8").contains(ProtocolMessage.STATE_UPDATE));
    assertTrue("Correct state not sent", new String(channel.getMessageList().get(0).getBuffer(), "UTF-8").contains(UpdateState.UPDATING.name()));
  }
  
  @Test
  public void testGetCurrentState() throws Exception {
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, false, UpdateState.IDLE, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4322), false, true, UpdateState.WAITING, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.IDLE, UpdateState.IDLE));
    
    LifecycleService service = new LifecycleService();
    service.members = members;
    
    assertTrue("Wrong State", service.getCurrentState() == UpdateState.WAITING);
  }
  
  @Test
  public void testAllEquals() throws Exception {
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, false, UpdateState.WAITING, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4322), false, true, UpdateState.WAITING, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.WAITING, UpdateState.IDLE));
    
    LifecycleService service = new LifecycleService();
    service.members = members;
    
    assertTrue("All should equal", service.allEquals(UpdateState.WAITING));
    assertTrue("All should not equal", !service.allEquals(UpdateState.UPDATING));
    
    members.get(1).setState(UpdateState.UPDATING);

    assertTrue("All should definitely not equal", !service.allEquals(UpdateState.WAITING));
    
  }
}
