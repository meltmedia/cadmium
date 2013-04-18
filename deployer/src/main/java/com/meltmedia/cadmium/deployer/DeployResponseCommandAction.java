package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.commands.AbstractCommandResponse;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Command action to hold responses to the deploy request.
 */
@Singleton
public class DeployResponseCommandAction extends AbstractCommandResponse<DeployResponse> implements CommandAction<DeployResponse> {
  private final Logger log = LoggerFactory.getLogger(getClass());

  public static final String COMMAND_ACTION = "DEPLOY_RESPONSE";

  @Override
  public String getName() {
    return COMMAND_ACTION;
  }

  @Override
  public boolean execute(CommandContext<DeployResponse> ctx) throws Exception {
    log.info("Received response for "+COMMAND_ACTION+" from {}", ctx.getSource());
    responses.put(new ChannelMember(ctx.getSource()), ctx.getMessage());
    return true;
  }

  @Override
  public void handleFailure(CommandContext<DeployResponse> ctx, Exception e) {
    log.error("Command Failed " + ToStringBuilder.reflectionToString(ctx), e);
  }
}
