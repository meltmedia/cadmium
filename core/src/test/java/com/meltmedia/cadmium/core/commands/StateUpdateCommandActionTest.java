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
package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.core.messaging.jgroups.DummyJChannel;
import com.meltmedia.cadmium.core.messaging.jgroups.JGroupsMessageSender;

public class StateUpdateCommandActionTest {

  @Test
  public void testCommandNotAllWait() throws Exception {
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
    members.add(new ChannelMember(new IpAddress(4322), true, false, UpdateState.WAITING));
    members.add(new ChannelMember(new IpAddress(1234), false, true, UpdateState.IDLE));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.IDLE));
    
    LifecycleService service = new LifecycleService();
    service.setMembers(members);
    service.setSender(sender);
    
    StateUpdateCommandAction cmd = new StateUpdateCommandAction();
    cmd.lifecycleService = service;
    
    CommandContext ctx = new CommandContext(other3, new Message());
    ctx.getMessage().setCommand(ProtocolMessage.STATE_UPDATE);
    ctx.getMessage().getProtocolParameters().put("state", UpdateState.WAITING.name());
    
    assertTrue("Command failed", cmd.execute(ctx));
    
    assertTrue("State not updated", service.getState(new ChannelMember(other3)) == UpdateState.WAITING);
    
  }

  @Test
  public void testCommandAllWait() throws Exception {
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
    members.add(new ChannelMember(new IpAddress(4322), true, false, UpdateState.WAITING));
    members.add(new ChannelMember(new IpAddress(1234), false, true, UpdateState.WAITING));
    members.add(new ChannelMember(new IpAddress(4321), false, false, UpdateState.IDLE));
    
    LifecycleService service = new LifecycleService();
    service.setMembers(members);
    service.setSender(sender);
    
    DummySiteDownService sd = new DummySiteDownService();
    DummyContentService content = new DummyContentService();
    
    StateUpdateCommandAction cmd = new StateUpdateCommandAction();
    cmd.fileServlet = content;
    cmd.maintFilter = sd;
    cmd.lifecycleService = service;
    
    CommandContext ctx = new CommandContext(other3, new Message());
    ctx.getMessage().setCommand(ProtocolMessage.STATE_UPDATE);
    ctx.getMessage().getProtocolParameters().put("state", UpdateState.WAITING.name());
    
    assertTrue("Command failed", cmd.execute(ctx));
    
    assertTrue("State not updated", service.getState(new ChannelMember(other3)) == UpdateState.WAITING);
    assertTrue("Site did not come down", sd.didStart);
    assertTrue("Site did not come back up", sd.didStop);
    assertTrue("Site did not change content", content.switched);
    
  }
}
