package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;

@Singleton
public class UpdateDoneCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected LifecycleService lifecycleService;

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    if(lifecycleService.getCurrentState() == UpdateState.UPDATING) {
      log.info("Update is done @ {}", ctx.getSource());
      lifecycleService.updateMyState(UpdateState.WAITING);
    } else {
      log.info("Received Update done message while not updating: {}", lifecycleService.getCurrentState());
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }
}
