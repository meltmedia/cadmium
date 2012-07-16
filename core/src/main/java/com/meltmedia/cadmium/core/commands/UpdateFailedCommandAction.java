package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class UpdateFailedCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected CoordinatedWorker worker;
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected HistoryManager historyManager;

  public String getName() { return ProtocolMessage.UPDATE_FAILED; }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    if(lifecycleService.getCurrentState() != UpdateState.IDLE) {
      log.info("update has failed @ {}", ctx.getSource());
      worker.killUpdate();
      lifecycleService.updateMyState(UpdateState.IDLE);
      if(historyManager != null) {
        String branch = "";
        if(ctx.getMessage().getProtocolParameters().containsKey("branch")) {
          branch = ctx.getMessage().getProtocolParameters().get("branch");
        }
        String sha = "";
        if(ctx.getMessage().getProtocolParameters().containsKey("sha")) {
          sha = ctx.getMessage().getProtocolParameters().get("sha");
        }
        String openId = "";
        if(ctx.getMessage().getProtocolParameters().containsKey("openId")) {
          openId = ctx.getMessage().getProtocolParameters().get("openId");
        }
        historyManager.logEvent(branch, sha, openId, "", "Update failed to run!", false, false);
      }
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }

}
