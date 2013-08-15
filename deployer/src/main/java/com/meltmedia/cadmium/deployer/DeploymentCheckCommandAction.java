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
package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Command Action that checks the state of a deployment.
 */
public class DeploymentCheckCommandAction implements CommandAction<DeploymentCheckRequest> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  public static final String COMMAND_ACTION = "DEPLOYMENT_CHECK_REQUEST";

  @Inject
  private MessageSender sender;

  @Inject
  private IJBossUtil jbossUtil;

  @Override
  public String getName() {
    return COMMAND_ACTION;
  }

  @Override
  public boolean execute(CommandContext<DeploymentCheckRequest> ctx) throws Exception {
    if(StringUtils.isNotBlank(ctx.getMessage().getBody().getWarName())){
      logger.debug("Checking deployment state.");
      String warName = ctx.getMessage().getBody().getWarName();
      DeploymentCheckResponse deploymentResponse = new DeploymentCheckResponse();
      deploymentResponse.setStarted(true);
      try {
        boolean deployed = jbossUtil.isWarDeployed(warName);
        logger.info("{} deployment state: {}", warName, deployed);
        deploymentResponse.setFinished(deployed);
      } catch(NoDeploymentFoundException e) {
        logger.info("No deployment has started yet.");
        deploymentResponse.setStarted(false);
      } catch(Throwable e) {
        logger.error("Failed to deploy "+warName, e);
        deploymentResponse.setError(e);
      }
      Message<DeploymentCheckResponse> response = new Message<DeploymentCheckResponse>(DeploymentCheckResponseCommandAction.COMMAND_ACTION, deploymentResponse);
      sender.sendMessage(response, null);
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext <DeploymentCheckRequest> ctx, Exception e) {
    logger.error("Failed to handle "+COMMAND_ACTION+" from "+ctx.getSource(), ToStringBuilder.reflectionToString(ctx));
    DeploymentCheckResponse deploymentResponse = new DeploymentCheckResponse();
    deploymentResponse.setError(e);
    Message<DeploymentCheckResponse> response = new Message<DeploymentCheckResponse>(DeploymentCheckResponseCommandAction.COMMAND_ACTION, deploymentResponse);
    try {
      sender.sendMessage(response, null);
    } catch(Exception e1) {
      logger.warn("Failed to send error response.", e1);
    }
  }
}
