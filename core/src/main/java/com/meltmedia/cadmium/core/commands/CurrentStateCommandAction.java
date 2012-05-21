package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.messaging.ChannelMember;

@Singleton
public class CurrentStateCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected LifecycleService lifecycleService;

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    log.info("Responding with current state {}", lifecycleService.getCurrentState());
    lifecycleService.sendStateUpdate(new ChannelMember(ctx.getSource()));
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }

}
