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

public class UpdateFailedCommandActionTest {

  @Test
  public void testCommand() throws Exception {
    DummyCoordinatedWorker worker = new DummyCoordinatedWorker();
    worker.updating = true;
    
    DummyMessageSender sender = new DummyMessageSender();
    
    LifecycleService service = new LifecycleService();
    
    service.setSender(sender);
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, true, UpdateState.UPDATING));
    service.setMembers(members);
    
    UpdateFailedCommandAction cmd = new UpdateFailedCommandAction();
    cmd.lifecycleService = service;
    cmd.worker = worker;
    
    CommandContext ctx = new CommandContext(new IpAddress(1234), new Message());
    ctx.getMessage().setCommand(ProtocolMessage.UPDATE_FAILED);
    
    assertTrue("Command returned false", cmd.execute(ctx));
    
    assertTrue("Update not killed", worker.killed && worker.updating);
    assertTrue("State not updated", service.getCurrentState() == UpdateState.IDLE);
    assertTrue("No message sent", sender.dest == null && sender.msg != null);
    assertTrue("No state_update msg sent", sender.msg.getCommand() == ProtocolMessage.STATE_UPDATE);
    assertTrue("Incorrect state sent in message", sender.msg.getProtocolParameters().containsKey("state") 
        && sender.msg.getProtocolParameters().get("state").equals(UpdateState.IDLE.name()));
  }
}
