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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;

public class DeployCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static String DEPLOY_ACTION = "DEPLOY";

  @Override
  public String getName() { return DEPLOY_ACTION; }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    log.info("Beginning Deploy Command, started by {}", ctx.getSource());
    Map<String,String> params = ctx.getMessage().getProtocolParameters();
    
    String domain = params.get("domain").trim();
    String branch = params.get("branch").trim();
    String repo = params.get("repo").trim();
    String context = params.get("context").trim();
    
    // make sure our state is OK.  We need some proper validation.
    if( domain.isEmpty() || branch.isEmpty() || repo.isEmpty() || context.isEmpty()) {
      log.warn("Invalid deploy request: Empty field. branch: {}, repo {}, domain {}, context {}", new String[]{branch, repo, domain, context});
      throw new Exception("Invalid deploy message.");
    }
    
    log.info("Beginning war creation. branch: {}, repo {}, domain {}, context {}", new String[]{branch, repo, domain, context});

    JBossUtil.addVirtualHost(domain, log);    
    
    //setup war name and inset into list in initCommand
    List<String> newWarNames = new ArrayList<String>();
    String tmpFileName = domain.replace("\\.", "_") + ".war";   
    File tmpZip = File.createTempFile(tmpFileName, null);
    tmpZip.deleteOnExit();
    newWarNames.add(tmpZip.getAbsolutePath());
    updateWar("cadmium-war.war", null, newWarNames, repo, branch, domain, context);
    
    String deployPath = System.getProperty("jboss.server.home.dir", "/opt/jboss/server/meltmedia") + "/deploy";
    
    tmpZip.renameTo(new File(deployPath, tmpFileName));
    
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {
    // TODO Auto-generated method stub

  }

}