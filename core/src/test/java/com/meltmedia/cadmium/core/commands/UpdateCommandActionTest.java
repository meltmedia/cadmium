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

public class UpdateCommandActionTest {
  
  @Test
  public void testCommand() throws Exception {
    DummyCoordinatedWorker<ContentUpdateRequest> worker = new DummyCoordinatedWorker<ContentUpdateRequest>();
    DummyMessageSender<StateUpdateRequest, Void> sender = new DummyMessageSender<StateUpdateRequest, Void>();
    
    LifecycleService service = new LifecycleService();
 
    
    service.setSender(sender);
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, true, UpdateState.IDLE, UpdateState.IDLE));
    service.setMembers(members);
    
    UpdateCommandAction updateCmd = new UpdateCommandAction();
    updateCmd.lifecycleService = service;
    updateCmd.worker = worker;
    
    ContentUpdateRequest request = new ContentUpdateRequest();
    CommandContext<ContentUpdateRequest> ctx = new CommandContext<ContentUpdateRequest>(new IpAddress(1234), new Message<ContentUpdateRequest>(ProtocolMessage.UPDATE, request));
    
    assertTrue("Command returned false", updateCmd.execute(ctx));
    
    assertTrue("Worker not properly called", worker.updating && !worker.killed);
    assertTrue("State not updated", service.getCurrentState() == UpdateState.UPDATING);
    assertTrue("No state update message sent", sender.dest == null && sender.msg != null 
        && sender.msg.getHeader().getCommand() == ProtocolMessage.STATE_UPDATE);
    assertEquals("Incorrect state sent in message", UpdateState.UPDATING.name(), sender.msg.getBody().getState());
  }
}
