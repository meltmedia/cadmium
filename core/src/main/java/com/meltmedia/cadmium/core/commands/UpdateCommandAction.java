package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;

@Singleton
public class UpdateCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
    
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected CoordinatedWorker worker;
  
  public UpdateCommandAction(){}

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    if(lifecycleService.getCurrentState() == UpdateState.IDLE) {
      log.info("Beginning an update, started by {}", ctx.getSource());
      lifecycleService.updateMyState(UpdateState.UPDATING);
      
      worker.beginPullUpdates(ctx.getMessage().getProtocolParameters());
      
    } else {
      log.info("Received UPDATE message with current state [{}] not IDLE from {}", lifecycleService.getCurrentState(), ctx.getSource());
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }
}
