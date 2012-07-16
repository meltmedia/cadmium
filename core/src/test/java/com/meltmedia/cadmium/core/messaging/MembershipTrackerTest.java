/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.messaging;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.messaging.jgroups.DummyJChannel;
import com.meltmedia.cadmium.core.messaging.jgroups.JGroupsMessageSender;

public class MembershipTrackerTest {

  @Test
  public void testNewMember() throws Exception {
    Address me = new IpAddress(12345);
    Address other = new IpAddress(54321);
    Address other2 = new IpAddress(345234);
    Address other3 = new IpAddress(36554);
    Vector<Address> viewMems = new Vector<Address>();
    viewMems.add(other2);
    viewMems.add(me);
    viewMems.add(other3);
    
    DummyJChannel channel = new DummyJChannel(me, viewMems);
    
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    ChannelMember oldMember = new ChannelMember(other, true, false);
    members.add(oldMember);
    
    Properties configProps = new Properties();
    
    JGroupsMessageSender sender = new JGroupsMessageSender();
    sender.setChannel(channel);
    
    new MembershipTracker(sender, channel, members, configProps);
    //tracker.viewAccepted(channel.getView());
    
    List<org.jgroups.Message> msgs = channel.getMessageList();
    
    assertTrue("Member not added", members.size() == 3);
    
    assertTrue("Member1 address is wrong", members.get(0).getAddress().toString().equals(other2.toString()));
    assertTrue("Member1 not coordinator", members.get(0).isCoordinator());
    assertTrue("Member1 not me", !members.get(0).isMine());
    
    assertTrue("Member2 address is wrong", members.get(1).getAddress().toString().equals(me.toString()));
    assertTrue("Member2 not coordinator", !members.get(1).isCoordinator());
    assertTrue("Member2 not me", members.get(1).isMine());

    assertTrue("Member3 address is wrong", members.get(2).getAddress().toString().equals(other3.toString()));
    assertTrue("Member3 not coordinator", !members.get(2).isCoordinator());
    assertTrue("Member3 not me", !members.get(2).isMine());
    
    assertTrue("Wrong number of messages sent", msgs.size() == 4);
    
    assertTrue("Wrong msg1 dest address", msgs.get(0).getDest().toString().equals(other2.toString()));
    assertTrue("Wrong msg1 msg", msgs.get(0).getObject().toString().contains(ProtocolMessage.CURRENT_STATE));

    assertTrue("Wrong msg2 dest address", msgs.get(1).getDest().toString().equals(me.toString()));
    assertTrue("Wrong msg2 msg", msgs.get(1).getObject().toString().contains(ProtocolMessage.CURRENT_STATE));

    assertTrue("Wrong msg2 dest address", msgs.get(2).getDest().toString().equals(other3.toString()));
    assertTrue("Wrong msg2 msg", msgs.get(2).getObject().toString().contains(ProtocolMessage.CURRENT_STATE));
    
    assertTrue("Wrong msg3 dest address", msgs.get(3).getDest().toString().equals(other2.toString()));
    assertTrue("Wrong msg3 msg", msgs.get(3).getObject().toString().contains(ProtocolMessage.SYNC));
  }
}
