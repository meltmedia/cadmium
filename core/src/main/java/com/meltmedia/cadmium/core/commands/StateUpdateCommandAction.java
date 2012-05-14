package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;

public class StateUpdateCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected SiteDownService maintFilter;
  
  @Inject
  protected ContentService fileServlet;

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    if(ctx.getMessage().getProtocolParameters().containsKey("state")) {
      lifecycleService.updateState(new ChannelMember(ctx.getSource()), UpdateState.valueOf(ctx.getMessage().getProtocolParameters().get("state")));
      if(lifecycleService.getCurrentState() == UpdateState.WAITING && lifecycleService.allEquals(UpdateState.WAITING)) {
        log.info("Done updating content now switching content.");
        maintFilter.start();
        fileServlet.switchContent();
        maintFilter.stop();
      }
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {
    log.warn("Failed to update state {}", e.getMessage());
  }

}
