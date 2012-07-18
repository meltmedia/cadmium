package com.meltmedia.cadmium.deployer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;

public class UndeployCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static String UNDEPLOY_ACTION = "UNDEPLOY";

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
    
    JBossUtil.undeploy(domain + "/" + contextRoot, log);
    
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {
    e.printStackTrace();
  }

}
