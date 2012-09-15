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

import static com.meltmedia.cadmium.deployer.JBossUtil.DeploymentState.DEPLOYED;
import static com.meltmedia.cadmium.deployer.JBossUtil.DeploymentState.DEPLOYING;
import static com.meltmedia.cadmium.deployer.JBossUtil.DeploymentState.ERROR;
import static com.meltmedia.cadmium.deployer.JBossUtil.DeploymentState.UNDEPLOYED;
import static com.meltmedia.cadmium.deployer.JBossUtil.DeploymentState.UNDEPLOYING;
import static com.meltmedia.cadmium.deployer.JBossUtil.DeploymentState.NOT_STARTED;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.deployer.JBossUtil.DeploymentState;

public class UndeployCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static String UNDEPLOY_ACTION = "UNDEPLOY";
  
  @Inject
  protected DeploymentTracker tracker;

  @Override
  public String getName() { return UNDEPLOY_ACTION; }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    log.info("Beginning Undeploy Command, started by {}", ctx.getSource());
    Map<String,String> params = ctx.getMessage().getProtocolParameters();
    
    String domain = params.get("domain");
    String contextRoot = params.get("context");
    log.debug("Undeploying domain {}, context {}", domain, contextRoot);
    if(domain.isEmpty() && contextRoot.isEmpty()) {
      log.info("Invalid undeployment request!");
      return false;
    }
    String warName = domain.replace("\\.", "_") + ".war";
    DeploymentState currentState = JBossUtil.getDeploymentState(warName);
    if(currentState == DEPLOYED) {
      JBossUtil.undeploy(domain + "/" + contextRoot, log);
    } else if(currentState == DEPLOYING || currentState == ERROR) {
      JBossUtil.forceDeleteCadmiumWar(log, warName);
    }
    
    tracker.initializeUndeployment(domain, contextRoot);
    
    return true;
  }

  public static void undeployWar(ChannelMember me, StatusTracker status,
      String warFileName, Logger log) throws Exception, IOException {
    //Check to see if war is already deployed.
    DeploymentState currentState = JBossUtil.getDeploymentState(warFileName);
    if(currentState == DEPLOYING || currentState == DEPLOYED || currentState == ERROR) {
      status.logToMember(me, "Deleting war: " + warFileName);
      JBossUtil.forceDeleteCadmiumWar(log, warFileName);
    }
    long timeOutTime = System.currentTimeMillis() + 60000l;
    while(timeOutTime > System.currentTimeMillis() && (currentState = JBossUtil.getDeploymentState(warFileName)) != UNDEPLOYING && currentState != UNDEPLOYED && currentState != NOT_STARTED) {
      status.logToMember(me, "JBoss Deployment State changed to: "+currentState);
      Thread.sleep(5000l);
    }
    
    timeOutTime = System.currentTimeMillis() + 60000l;
    while(timeOutTime > System.currentTimeMillis() && (currentState = JBossUtil.getDeploymentState(warFileName)) == UNDEPLOYING) {
      status.logToMember(me, "JBoss Deployment State changed to: "+currentState);
      Thread.sleep(5000l);
    }
    status.logToMember(me, "JBoss Deployment State changed to: "+currentState);
    if(currentState == UNDEPLOYED) {
      status.logToMember(me, "Existing war has been undeployed.");
    }
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {
    e.printStackTrace();
  }

}
