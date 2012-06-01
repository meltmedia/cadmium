package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class MaintenanceCommandActionTest {

	@Test
	public void testCommand() throws Exception {
		DummyCoordinatedWorker worker = new DummyCoordinatedWorker();
		DummyMessageSender sender = new DummyMessageSender();
		DummySiteDownService siteDownService = new DummySiteDownService();
		
		
		LifecycleService service = new LifecycleService();
		
		service.setSender(sender);
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, true, UpdateState.IDLE));
    service.setMembers(members);
    
    MaintenanceCommandAction maintCmd = new MaintenanceCommandAction();
    maintCmd.siteDownService = siteDownService;
    maintCmd.manager = new HistoryManager(null);
    CommandContext ctx = new CommandContext(new IpAddress(1234), new Message());
    ctx.getMessage().setCommand(ProtocolMessage.MAINTENANCE);
    ctx.getMessage().getProtocolParameters().put("state", "on");
    ctx.getMessage().getProtocolParameters().put("comment", "comment");
    
    assertTrue("Command failed", maintCmd.execute(ctx));
    assertTrue("Site Down is On", siteDownService.isOn());
    
    ctx.getMessage().getProtocolParameters().put("state", "off");
    assertTrue("Command failed", maintCmd.execute(ctx));
    Assert.assertEquals("Site Down is Off", false, siteDownService.isOn());
   	
	}
	
}
