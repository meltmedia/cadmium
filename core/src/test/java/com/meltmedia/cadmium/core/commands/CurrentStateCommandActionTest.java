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
package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class CurrentStateCommandActionTest {
  
  @Test
  public void testCommand() throws Exception {
    DummyMessageSender<StateUpdateRequest, StateUpdateRequest> sender = new DummyMessageSender<StateUpdateRequest, StateUpdateRequest>();
    
    LifecycleService service = new LifecycleService();
    
    service.setSender(sender);
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, true, UpdateState.UPDATING, UpdateState.WAITING));
    service.setMembers(members);
    
    CurrentStateCommandAction cmd = new CurrentStateCommandAction();
    cmd.lifecycleService = service;
    
    CommandContext<Void> ctx = new CommandContext<Void>(new IpAddress(1234), new Message<Void>());
    
    assertTrue("Command returned false", cmd.execute(ctx));
    
    assertTrue("Message not sent", sender.msg != null);
    assertTrue("Destination not set correctly", sender.dest != null && sender.dest.getAddress() == ctx.getSource());
    assertTrue("message not state_update", sender.msg.getHeader().getCommand() == ProtocolMessage.STATE_UPDATE);
    assertNotNull(sender.msg.getBody());
    assertNotNull(sender.msg.getBody().getState());
    assertEquals("State not correct in message", UpdateState.UPDATING.name(), sender.msg.getBody().getState());

    
    assertTrue("Second message not sent", sender.msg2 != null);
    assertTrue("Second destination not set correctly", sender.dest2 != null && sender.dest2.getAddress() == ctx.getSource());
    assertTrue("Second message not state_update", sender.msg2.getHeader().getCommand() == ProtocolMessage.STATE_UPDATE);
    assertNotNull(sender.msg2.getBody());
    assertNotNull(sender.msg2.getBody().getConfigState());
    assertEquals("State not correct in message", UpdateState.WAITING.name(), sender.msg2.getBody().getConfigState());
    
  }

}
