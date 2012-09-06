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
package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
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
  
  @Inject
  protected HistoryManager historyManager;
  
  @Inject
  protected ConfigManager configManager;

  public String getName() { return ProtocolMessage.STATE_UPDATE; }
  
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
          setHistoryDone(ctx);
          maintFilter.stop();
          lifecycleService.updateMyState(UpdateState.IDLE, ctx.getMessage().getProtocolParameters().get("uuid"));
        }
      } else if(ctx.getMessage().getProtocolParameters().containsKey("configState")) {
        UpdateState newState = UpdateState.valueOf(ctx.getMessage().getProtocolParameters().get("configState"));
        if(newState != UpdateState.UPDATING || !lifecycleService.isMe(new ChannelMember(ctx.getSource())) || lifecycleService.getCurrentConfigState() != UpdateState.WAITING) {
          lifecycleService.updateConfigState(new ChannelMember(ctx.getSource()), newState);
        }
        if(lifecycleService.getCurrentConfigState() == UpdateState.WAITING && lifecycleService.allEqualsConfig(UpdateState.WAITING)) {
          log.info("Done updating config now switching config.");
          
          configManager.makeConfigParserLive();
          
          setHistoryDone(ctx);
          lifecycleService.updateMyConfigState(UpdateState.IDLE, ctx.getMessage().getProtocolParameters().get("uuid"));
        }
      }
    } catch(Exception e) {
      log.warn("Failed to run state update command action", e);
      return false;
    }
    return true;
  }

  private void setHistoryDone(CommandContext ctx) {
    if(ctx.getMessage().getProtocolParameters().containsKey("uuid")) {
      historyManager.markHistoryEntryAsFinished(ctx.getMessage().getProtocolParameters().get("uuid"));
    }
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {
    log.warn("Failed to update state");
  }

}
