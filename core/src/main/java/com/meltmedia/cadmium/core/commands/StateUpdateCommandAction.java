package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;

@Singleton
public class StateUpdateCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected SiteDownService maintFilter;
  
  @Inject
  protected ContentService fileServlet;
  
  @Inject
  protected SiteConfigProcessor processor;

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    try {
      if(ctx.getMessage().getProtocolParameters().containsKey("state")) {
        UpdateState newState = UpdateState.valueOf(ctx.getMessage().getProtocolParameters().get("state"));
        if(newState != UpdateState.UPDATING || !lifecycleService.isMe(new ChannelMember(ctx.getSource())) || lifecycleService.getCurrentState() != UpdateState.WAITING) {
          lifecycleService.updateState(new ChannelMember(ctx.getSource()), newState);
        }
        if(lifecycleService.getCurrentState() == UpdateState.WAITING && lifecycleService.allEquals(UpdateState.WAITING)) {
          log.info("Done updating content now switching content.");
          maintFilter.start();
          fileServlet.switchContent(ctx.getMessage().getRequestTime());
          if(processor != null) {
            processor.makeLive();
          }
          maintFilter.stop();
          lifecycleService.updateMyState(UpdateState.IDLE);
        }
      }
    } catch(Exception e) {
      log.warn("Failed to run state update command action", e);
      return false;
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {
    log.warn("Failed to update state");
  }

}
