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
public class LoggerConfigResponseCommandAction extends AbstractCommandResponse<LoggerConfigResponse> implements
    CommandAction<LoggerConfigResponse> {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public String getName() {return ProtocolMessage.LOGGER_CONFIG_RESPONSE;}

  @Override
  public boolean execute(CommandContext<LoggerConfigResponse> ctx)
      throws Exception {
    log.trace("Received Logger config response: {}", ctx);
    if(ctx.getMessage().getBody() != null && ctx.getMessage().getBody().getLoggers() != null) {
      this.responses.put(new ChannelMember(ctx.getSource()), ctx.getMessage());
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext<LoggerConfigResponse> ctx, Exception e) {
    log.error("Command Failed "+ToStringBuilder.reflectionToString(ctx), e);
  }

}
