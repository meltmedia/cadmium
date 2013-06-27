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

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.history.loggly.Event;
import com.meltmedia.cadmium.core.history.loggly.EventQueue;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import junit.framework.Assert;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MaintenanceCommandActionTest {

	@Test
	public void testCommand() throws Exception {
		DummyMessageSender<Void, Void> sender = new DummyMessageSender<Void, Void>();
		DummySiteDownService siteDownService = new DummySiteDownService();
		
		
		LifecycleService service = new LifecycleService();
		
		service.setSender(sender);
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, true, UpdateState.IDLE, UpdateState.IDLE));
    service.setMembers(members);
    
    MaintenanceCommandAction maintCmd = new MaintenanceCommandAction();
    maintCmd.siteDownService = siteDownService;
    EventQueue queue = mock(EventQueue.class);
    doNothing().when(queue).log(any(Event.class));
    maintCmd.manager = new HistoryManager(null, Executors.newSingleThreadExecutor(), queue);
    MaintenanceRequest request = new MaintenanceRequest();
    request.setState("on");
    request.setComment("comment");
    CommandContext<MaintenanceRequest> ctx = new CommandContext<MaintenanceRequest>(new IpAddress(1234), new Message<MaintenanceRequest>(ProtocolMessage.MAINTENANCE, request));
    
    assertTrue("Command failed", maintCmd.execute(ctx));
    assertTrue("Site Down is On", siteDownService.isOn());
    
    ctx.getMessage().getBody().setState("off");
    assertTrue("Command failed", maintCmd.execute(ctx));
    Assert.assertEquals("Site Down is Off", false, siteDownService.isOn());
    verify(queue, times(2)).log(any(Event.class));
   	
	}
	
}
