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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.LoggerConfig;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.core.util.LogUtils;

@Singleton
public class LoggerConfigRequestCommandAction implements
    CommandAction<LoggerConfigRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected MessageSender sender;

  @Override
  public String getName() {return ProtocolMessage.LOGGER_CONFIG_REQUEST;}

  @Override
  public boolean execute(CommandContext<LoggerConfigRequest> ctx)
      throws Exception {
    try {
      if(ctx.getMessage().getBody() != null) {
        log.trace("Received request {}", ctx.getMessage().getBody());
        String name = ctx.getMessage().getBody().getLoggerName();
        String level = ctx.getMessage().getBody().getLevel();
        if(StringUtils.isBlank(level)) {
          log.trace("Looking up loggers...");
          LoggerConfigResponse resp = new LoggerConfigResponse();
          resp.setLoggers(filterLoggerName(name, LogUtils.getConfiguredLoggers()));
          log.trace("Creating response...");
          Message<LoggerConfigResponse> msg = new Message<LoggerConfigResponse>(ProtocolMessage.LOGGER_CONFIG_RESPONSE, resp);
          try {
            log.trace("Sending response...");
            sender.sendMessage(msg, new ChannelMember(ctx.getSource()));
          } catch(Throwable t) {
            log.warn("Failed to send response.", t);
          }
        } else if(StringUtils.isNotBlank(name)){
          log.trace("Setting logger {} with level {}", name, level);
          LoggerConfigResponse resp = new LoggerConfigResponse();
          resp.setLoggers(LogUtils.setLogLevel(name, level));
          Message<LoggerConfigResponse> msg = new Message<LoggerConfigResponse>(ProtocolMessage.LOGGER_CONFIG_RESPONSE, resp);
          try {
            sender.sendMessage(msg, new ChannelMember(ctx.getSource()));
          } catch(Throwable t) {
            log.warn("Failed to send response.", t);
          }
        }
      }
    } catch(Throwable t) {
      log.error("Failed to process logger config request: "+ctx, t);
      LoggerConfigResponse resp = new LoggerConfigResponse();
      Message<LoggerConfigResponse> msg = new Message<LoggerConfigResponse>(ProtocolMessage.LOGGER_CONFIG_RESPONSE, resp);
      try {
        sender.sendMessage(msg, new ChannelMember(ctx.getSource()));
      } catch(Throwable t1) {
        log.warn("Failed to send response.", t1);
      }
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext<LoggerConfigRequest> ctx, Exception e) {
    log.error("Command Failed "+ToStringBuilder.reflectionToString(ctx), e);
  }
  
  private LoggerConfig[] filterLoggerName(String name, LoggerConfig[] configs) {
    log.trace("Filtering {} loggers with name {}", configs.length, name);
    if(StringUtils.isBlank(name)) {
      log.trace("Found {} loggers", configs.length);
      return configs;
    }
    for(LoggerConfig config : configs) {
      if(config.getName().equals(name)) {
        log.trace("Returning logger {}", config);
        return new LoggerConfig[] {config};
      }
    }
    log.trace("Responding with no loggers.");
    return new LoggerConfig[] {};
  }

}
