package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;

public class UpdateFailedCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected CoordinatedWorker worker;
  
  @Inject
  protected LifecycleService lifecycleService;

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    if(lifecycleService.getCurrentState() != UpdateState.IDLE) {
      log.info("update has failed @ {}", ctx.getSource());
      worker.killUpdate();
      lifecycleService.updateMyState(UpdateState.IDLE);
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }

}
