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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.history.HistoryEntry;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class HistoryResponseCommandActionTest {

  @Test
  public void testCommand() throws Exception {
    HistoryResponseCommandAction command = new HistoryResponseCommandAction();
    
    ChannelMember mem = new ChannelMember(new IpAddress(5432));
    
    HistoryResponse response = new HistoryResponse(new ArrayList<HistoryEntry>());
    Message<HistoryResponse> msg = new Message<HistoryResponse>(ProtocolMessage.HISTORY_RESPONSE, response);
    
    CommandContext<HistoryResponse> ctx = new CommandContext<HistoryResponse>(mem.getAddress(), msg);
    
    command.execute(ctx);
    
    assertTrue("No responses captured", !command.responses.isEmpty());
    assertTrue("Response captured incorrectly", command.getResponse(mem) != null);
    assertTrue("Wrong message captured", command.getResponse(mem) == msg);
  }
}
