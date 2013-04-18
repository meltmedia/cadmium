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
 * Command Action that records the state of a deployment.
 */
@Singleton
public class DeploymentCheckResponseCommandAction extends AbstractCommandResponse<DeploymentCheckResponse> implements CommandAction<DeploymentCheckResponse> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  public static final String COMMAND_ACTION = "DEPLOYMENT_CHECK_RESPONSE";

  @Override
  public String getName() {
    return COMMAND_ACTION;
  }

  @Override
  public boolean execute(CommandContext<DeploymentCheckResponse> ctx) throws Exception {
    responses.put(new ChannelMember(ctx.getSource()), ctx.getMessage());
    return true;
  }

  @Override
  public void handleFailure(CommandContext<DeploymentCheckResponse> ctx, Exception e) {
    logger.error("Failed to handle "+COMMAND_ACTION+" from "+ctx.getSource(), ToStringBuilder.reflectionToString(ctx));
  }
}
