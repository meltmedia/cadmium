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

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.core.util.WarUtils;

public class WarInfoCommandAction implements CommandAction<WarInfoRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected MessageSender sender;
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected ConfigManager configManager;

  @Override
  public String getName() {return ProtocolMessage.WAR_INFO;}

  @Override
  public boolean execute(CommandContext<WarInfoRequest> ctx) throws Exception {
    if(ctx.getMessage().getBody().getWarInfo() == null) {
      Message<WarInfoRequest> stateMsg = new Message<WarInfoRequest>(ProtocolMessage.WAR_INFO, new WarInfoRequest());
      stateMsg.getBody().setWarInfo(WarUtils.getWarInfo(new File(System.getProperty("jboss.server.home.dir")+"/deploy", configManager.getWarFileName())));
      log.debug("Responing to {}:WAR_INFO request with info: {}", ctx.getSource(), ToStringBuilder.reflectionToString(stateMsg.getBody().getWarInfo()));
      try {
        sender.sendMessage(stateMsg, new ChannelMember(ctx.getSource()));
      } catch (Exception e) {
        log.error("Failed to send message to check for peir's WAR_INFO", e);
      }
    } else {
      log.debug("Updating {}:WAR_INFO to {}", ctx.getSource(), ToStringBuilder.reflectionToString(ctx.getMessage().getBody().getWarInfo()));
      lifecycleService.updateWarInfo(new ChannelMember(ctx.getSource()), ctx.getMessage().getBody().getWarInfo());
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext<WarInfoRequest> ctx, Exception e) {
    log.error("Command Failed "+ToStringBuilder.reflectionToString(ctx), e);
  }

}
