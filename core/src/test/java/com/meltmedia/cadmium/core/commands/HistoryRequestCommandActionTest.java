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

import java.util.Date;

import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.history.HistoryEntry;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.history.HistoryEntry.EntryType;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class HistoryRequestCommandActionTest {
  
  @SuppressWarnings("resource")
  @Test
  public void testCommand() throws Exception {
    DummyMessageSender<HistoryResponse, Void> sender = new DummyMessageSender<HistoryResponse, Void>();
    HistoryManager historyManager = new HistoryManager(null);
    historyManager.getHistory().add(new HistoryEntry(EntryType.CONTENT, new Date(), "", "master", "sha", 3000l, "me", "dir", true, "comment"));
    historyManager.getHistory().add(new HistoryEntry(EntryType.CONTENT, new Date(), "", "master", "sha1", 3000l, "me", "dir_1", true, "comment1"));
    historyManager.getHistory().add(new HistoryEntry(EntryType.CONTENT, new Date(), "", "master", "sha2", 3000l, "me", "dir_2", false, "comment2"));
    historyManager.getHistory().add(new HistoryEntry(EntryType.CONTENT, new Date(), "", "master", "sha3", 3000l, "me", "dir_3", false, "comment3"));
    
    HistoryRequestCommandAction command = new HistoryRequestCommandAction();
    command.sender = sender;
    command.historyManager = historyManager;
    
    HistoryRequest request = new HistoryRequest();
    request.setLimit(1);
    request.setFilter(true);
    Message<HistoryRequest> msg = new Message<HistoryRequest>(ProtocolMessage.HISTORY_REQUEST, request);
    
    CommandContext<HistoryRequest> ctx = new CommandContext<HistoryRequest>(new IpAddress(5432), msg);
    command.execute(ctx);
    
    assertTrue("Response message was not sent", sender.msg != null && sender.dest != null);
    assertTrue("Wrong type of message sent", sender.msg.getHeader().getCommand() == ProtocolMessage.HISTORY_RESPONSE);
    assertEquals("Incorrect history in reply.", 1, sender.msg.getBody().getHistory().size());
  }
}
