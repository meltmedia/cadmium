package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class UpdateDoneCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected LifecycleService lifecycleService;

  public String getName() { return ProtocolMessage.UPDATE_DONE; }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    log.info("Update is done @ {}, my state {}", ctx.getSource(), lifecycleService.getCurrentState());
    lifecycleService.sendStateUpdate(null);
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }
}
