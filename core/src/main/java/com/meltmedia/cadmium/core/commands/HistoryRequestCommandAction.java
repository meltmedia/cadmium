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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.history.HistoryEntry;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class HistoryRequestCommandAction implements CommandAction<HistoryRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());  
  
  @Inject
  protected HistoryManager historyManager;
  
  @Inject
  protected MessageSender sender;
  
  public String getName() { return ProtocolMessage.HISTORY_REQUEST; };

  @Override
  public boolean execute(CommandContext<HistoryRequest> ctx) throws Exception {
    HistoryRequest request = ctx.getMessage().getBody();
    if( request.getLimit() == null ) request.setLimit(-1);
    List<HistoryEntry> history = historyManager.getHistory(request.getLimit(), request.getFilter());
    log.info("Received history-request message responding with {} history items", history.size());
    
    HistoryResponse responseBody = new HistoryResponse(history);
    Message<HistoryResponse> response = new Message<HistoryResponse>(ProtocolMessage.HISTORY_RESPONSE, responseBody);
    sender.sendMessage(response, new ChannelMember(ctx.getSource()));
    return true;
  }

  @Override
  public void handleFailure(CommandContext<HistoryRequest> ctx, Exception e) {

  }

}
