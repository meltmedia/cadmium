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

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.maven.ArtifactResolver;

public class DeployCommandAction implements CommandAction<DeployRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static String DEPLOY_ACTION = "DEPLOY";

  @Override
  public String getName() { return DEPLOY_ACTION; }
  
  @Inject
  protected ArtifactResolver artifactResolver;

  @Override
  public boolean execute(CommandContext<DeployRequest> ctx) throws Exception {
    log.info("Beginning Deploy Command, started by {}", ctx.getSource());
    DeployRequest request = ctx.getMessage().getBody();
    
    // make sure our state is OK.  We need some proper validation.
    if( StringUtils.isEmptyOrNull(request.getDomain()) || 
        StringUtils.isEmptyOrNull(request.getBranch()) || 
        StringUtils.isEmptyOrNull(request.getRepo()) || 
        StringUtils.isEmptyOrNull(request.getConfigBranch()) || 
        StringUtils.isEmptyOrNull(request.getContext())) {
      log.warn("Invalid deploy request: Empty field. {}", request);
      throw new Exception("Invalid deploy message.");
    }
    
    log.info("Beginning war creation. {}", request);

    JBossUtil.addVirtualHost(request.getDomain(), log);    
    
    //setup war name and inset into list in initCommand
    List<String> newWarNames = new ArrayList<String>();
    String tmpFileName = request.getDomain().replace("\\.", "_") + ".war";   
    File tmpZip = File.createTempFile(tmpFileName, null);
    tmpZip.deleteOnExit();
    newWarNames.add(tmpZip.getAbsolutePath());
    boolean secure = request.getSecure();
    
    // If the shiro config file can't be read lets not make this war secure.
    if(secure) {
      log.info("Requested to deploy secure app!");
      File shiroIniFile = new File(System.getProperty("com.meltmedia.cadmium.contentRoot"), "shiro.ini");
      if(!shiroIniFile.canRead()) {
        log.warn("Not adding security to war! The shiro config file \"{}\": either doesn't exist or cannot be read.", shiroIniFile.getAbsoluteFile());
        secure = false;
      } else {
          log.info("Found 'shiro' configuration making app secure.");
      }
    }
    
    File artifactFile = artifactResolver.resolveMavenArtifact(request.getArtifact());

    log.info("Artifact downloaded...  configuring war for deployment.");
    
    updateWar(null, artifactFile.getAbsolutePath(), newWarNames, request.getRepo(), request.getBranch(), request.getConfigRepo(), request.getConfigBranch(), request.getDomain(), request.getContext(), secure, log);
    
    String deployPath = System.getProperty("jboss.server.home.dir", "/opt/jboss/server/meltmedia") + "/deploy";

    log.info("Moving war {} into deployment directory {}", tmpFileName, deployPath);

    File newWar = new File(deployPath, tmpFileName);

    FileUtils.copyFile(tmpZip, newWar);

    FileUtils.deleteQuietly(tmpZip);

    log.info("Waiting for jBoss to pick up new deployment.");
    
    return true;
  }

  @Override
  public void handleFailure(CommandContext<DeployRequest> ctx, Exception e) {
    log.error("Failed to deploy "+ctx.getMessage(), e);
  }

}
