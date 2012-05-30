package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.assertTrue;

import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class HistoryResponseCommandActionTest {

  @Test
  public void testCommand() throws Exception {
    HistoryResponseCommandAction command = new HistoryResponseCommandAction();
    
    ChannelMember mem = new ChannelMember(new IpAddress(5432));
    
    Message msg = new Message();
    msg.setCommand(ProtocolMessage.HISTORY_RESPONSE);
    msg.getProtocolParameters().put("history", "history");
    
    CommandContext ctx = new CommandContext(mem.getAddress(), msg);
    
    command.execute(ctx);
    
    assertTrue("No responses captured", !command.responses.isEmpty());
    assertTrue("Response captured incorrectly", command.getResponse(mem) != null);
    assertTrue("Wrong message captured", command.getResponse(mem) == msg);
  }
}
