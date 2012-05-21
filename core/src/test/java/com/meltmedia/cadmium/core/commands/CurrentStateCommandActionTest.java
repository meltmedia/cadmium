package com.meltmedia.cadmium.core.commands;

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
    DummyMessageSender sender = new DummyMessageSender();
    
    LifecycleService service = new LifecycleService();
    
    service.setSender(sender);
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, true, UpdateState.UPDATING));
    service.setMembers(members);
    
    CurrentStateCommandAction cmd = new CurrentStateCommandAction();
    cmd.lifecycleService = service;
    
    CommandContext ctx = new CommandContext(new IpAddress(1234), new Message());
    
    assertTrue("Command returned false", cmd.execute(ctx));
    
    assertTrue("Message not sent", sender.msg != null);
    assertTrue("Destination not set correctly", sender.dest != null && sender.dest.getAddress() == ctx.getSource());
    assertTrue("message not state_update", sender.msg.getCommand() == ProtocolMessage.STATE_UPDATE);
    assertTrue("State not correct in message", sender.msg.getProtocolParameters().containsKey("state")
        && sender.msg.getProtocolParameters().get("state").equals(UpdateState.UPDATING.name()));
    
  }

}
