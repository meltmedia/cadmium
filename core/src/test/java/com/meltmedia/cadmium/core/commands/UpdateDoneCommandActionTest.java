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

public class UpdateDoneCommandActionTest {

  @Test
  public void testCommand() throws Exception {
    DummyMessageSender sender = new DummyMessageSender();
    
    LifecycleService service = new LifecycleService();
    
    service.setSender(sender);
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, true, UpdateState.UPDATING));
    service.setMembers(members);
    
    UpdateDoneCommandAction cmd = new UpdateDoneCommandAction();
    cmd.lifecycleService = service;
    
    CommandContext ctx = new CommandContext(new IpAddress(1234), new Message());
    ctx.getMessage().setCommand(ProtocolMessage.UPDATE_DONE);
    
    assertTrue("Command returned false", cmd.execute(ctx));
    
    assertTrue("State not updated", service.getCurrentState() == UpdateState.WAITING);
    assertTrue("no message sent", sender.dest == null && sender.msg != null);
    assertTrue("No state update", sender.msg.getCommand() == ProtocolMessage.STATE_UPDATE);
    assertTrue("Incorrect state in update", sender.msg.getProtocolParameters().containsKey("state") 
        && sender.msg.getProtocolParameters().get("state").equals(UpdateState.WAITING.name()));
  }
}
