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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;

public class UndeployCommandAction implements CommandAction<UndeployRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static String UNDEPLOY_ACTION = "UNDEPLOY";

  @Override
  public String getName() { return UNDEPLOY_ACTION; }

  @Override
  public boolean execute(CommandContext<UndeployRequest> ctx) throws Exception {
    log.info("Beginning Undeploy Command, started by {}", ctx.getSource());
    UndeployRequest request = ctx.getMessage().getBody();
    
    String domain = request.getDomain();
    String contextRoot = request.getContext();
    log.debug("Undeploying domain {}, context {}", domain, contextRoot);
    if(domain.isEmpty() && contextRoot.isEmpty()) {
      log.info("Invalid undeployment request!");
      return false;
    }
    
    JBossUtil.undeploy(domain + "/" + contextRoot, log);
    
    return true;
  }

  @Override
  public void handleFailure(CommandContext<UndeployRequest> ctx, Exception e) {
    e.printStackTrace();
  }

}
