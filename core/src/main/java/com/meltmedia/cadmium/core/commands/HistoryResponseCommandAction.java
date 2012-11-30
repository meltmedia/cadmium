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

import javax.inject.Singleton;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class HistoryResponseCommandAction extends AbstractCommandResponse<HistoryResponse> implements CommandAction<HistoryResponse> {
  private final Logger log = LoggerFactory.getLogger(getClass()); 

  public String getName() { return ProtocolMessage.HISTORY_RESPONSE; };

  @Override
  public boolean execute(CommandContext<HistoryResponse> ctx) throws Exception {
    log.info("Recevied response for HISTORY_REQUEST from {}", ctx.getSource());
    responses.put(new ChannelMember(ctx.getSource()), ctx.getMessage());
    return true;
  }

  @Override
  public void handleFailure(CommandContext<HistoryResponse> ctx, Exception e) {
    log.error("Command Failed "+ToStringBuilder.reflectionToString(ctx), e);
  }

}
