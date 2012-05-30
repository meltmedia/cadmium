package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.history.HistoryEntry;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class HistoryRequestCommandActionTest {
  @Test
  public void testCommand() throws Exception {
    DummyMessageSender sender = new DummyMessageSender();
    HistoryManager historyManager = new HistoryManager(null);
    historyManager.getHistory().add(new HistoryEntry(new Date(), "master", "sha", 3000l, "me", "dir", true, "comment"));
    historyManager.getHistory().add(new HistoryEntry(new Date(), "master", "sha1", 3000l, "me", "dir_1", true, "comment1"));
    historyManager.getHistory().add(new HistoryEntry(new Date(), "master", "sha2", 3000l, "me", "dir_2", false, "comment2"));
    historyManager.getHistory().add(new HistoryEntry(new Date(), "master", "sha3", 3000l, "me", "dir_3", false, "comment3"));
    
    HistoryRequestCommandAction command = new HistoryRequestCommandAction();
    command.sender = sender;
    command.historyManager = historyManager;
    
    Message msg = new Message();
    msg.setCommand(ProtocolMessage.HISTORY_REQUEST);
    msg.getProtocolParameters().put("limit", "1");
    msg.getProtocolParameters().put("filter", "true");
    
    CommandContext ctx = new CommandContext(new IpAddress(5432), msg);
    command.execute(ctx);
    
    assertTrue("Response message was not sent", sender.msg != null && sender.dest != null);
    assertTrue("Wrong type of message sent", sender.msg.getCommand() == ProtocolMessage.HISTORY_RESPONSE);
    assertTrue("No history in reply", sender.msg.getProtocolParameters().containsKey("history"));
  }
}
