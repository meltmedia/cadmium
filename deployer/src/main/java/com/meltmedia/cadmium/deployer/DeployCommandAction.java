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

import static com.meltmedia.cadmium.core.util.WarUtils.updateWar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import com.meltmedia.cadmium.deployer.DeploymentTracker.DeploymentStatus;
import com.meltmedia.cadmium.maven.ArtifactResolver;

public class DeployCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static String DEPLOY_ACTION = "DEPLOY";

  @Override
  public String getName() { return DEPLOY_ACTION; }
  
  @Inject
  protected ArtifactResolver artifactResolver;
  
  @Inject
  protected DeploymentTracker tracker;
  
  @Inject
  protected MembershipTracker memberTracker;

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    log.info("Beginning Deploy Command, started by {}", ctx.getSource());
    Map<String,String> params = ctx.getMessage().getProtocolParameters();
    ChannelMember me = memberTracker.getMe();
    
    String domain = params.get("domain").trim();
    String branch = params.get("branch").trim();
    String repo = params.get("repo").trim();
    String context = params.get("context").trim();
    String artifact = params.get("artifact").trim();
    
    // make sure our state is OK.  We need some proper validation.
    if( domain.isEmpty() || branch.isEmpty() || repo.isEmpty() || context.isEmpty()) {
      log.warn("Invalid deploy request: Empty field. branch: {}, repo {}, domain {}, context {}", new String[]{branch, repo, domain, context});
      throw new Exception("Invalid deploy message.");
    }
    DeploymentStatus status = tracker.initializeDeployment(domain, context);
    status.getMemberLogs(me);
    status.setMemberStarted(me);
    String warName = null;
    try {
      String tmpFileName = domain.replace("\\.", "_") + ".war"; 
      
      UndeployCommandAction.undeployWar(me, status, tmpFileName, log);
      
      status.logToMember(me, "Beginning war creation with repo "+repo+", branch "+branch+", domain "+domain+", context "+context);
      
      
      log.info("Beginning war creation. branch: {}, repo {}, domain {}, context {}", new String[]{branch, repo, domain, context});
      
      //setup war name and inset into list in initCommand
      List<String> newWarNames = new ArrayList<String>();  
      File tmpZip = File.createTempFile(tmpFileName, null);
      tmpZip.deleteOnExit();
      newWarNames.add(tmpZip.getAbsolutePath());
      boolean secure = new Boolean(params.get("secure"));
      
      // If the shiro config file can't be read lets not make this war secure.
      if(secure) {
        File shiroIniFile = new File(System.getProperty("com.meltmedia.cadmium.contentRoot"), "shiro.ini");
        if(!shiroIniFile.canRead()) {
          log.warn("Not adding security to war! The shiro config file \"{}\": either doesn't exist or cannot be read.", shiroIniFile.getAbsoluteFile());
          secure = false;
        }
      }
      
      status.logToMember(me, "Fetching maven artifact ["+artifact+"] from maven repository....");
      File artifactFile = artifactResolver.resolveMavenArtifact(artifact);
      status.logToMember(me, "Done, fetching maven artifact ["+artifact+"] from maven repository.");
  
      JBossUtil.addVirtualHost(domain, log); 
      status.logToMember(me, "Added vHost to JBoss.");
      
      status.logToMember(me, "Updating war...");
      updateWar(null, artifactFile.getAbsolutePath(), newWarNames, repo, branch, domain, context, secure);
      
      String deployPath = System.getProperty("jboss.server.home.dir", "/opt/jboss/server/meltmedia") + "/deploy";
      status.logToMember(me, "Moving war to deploy directory: "+deployPath);
      
      tmpZip.renameTo(new File(deployPath, tmpFileName));
      warName = tmpFileName;
      
      status.logToMember(me, "Waiting for JBoss to deploy war...");
      status.setWaiting(true);
    } catch(Throwable t) {
      status.setException(new CadmiumDeploymentException("Deployment failed.", t));
      if(warName != null) {
        JBossUtil.forceDeleteCadmiumWar(log, warName);
      }
      throw new Exception(t);
    }
    
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }

}
