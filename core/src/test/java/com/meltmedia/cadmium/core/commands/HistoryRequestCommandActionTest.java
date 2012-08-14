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
  
  @SuppressWarnings("resource")
  @Test
  public void testCommand() throws Exception {
    DummyMessageSender sender = new DummyMessageSender();
    HistoryManager historyManager = new HistoryManager(null);
    historyManager.getHistory().add(new HistoryEntry(new Date(), "", "master", "sha", 3000l, "me", "dir", true, "comment"));
    historyManager.getHistory().add(new HistoryEntry(new Date(), "", "master", "sha1", 3000l, "me", "dir_1", true, "comment1"));
    historyManager.getHistory().add(new HistoryEntry(new Date(), "", "master", "sha2", 3000l, "me", "dir_2", false, "comment2"));
    historyManager.getHistory().add(new HistoryEntry(new Date(), "", "master", "sha3", 3000l, "me", "dir_3", false, "comment3"));
    
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
