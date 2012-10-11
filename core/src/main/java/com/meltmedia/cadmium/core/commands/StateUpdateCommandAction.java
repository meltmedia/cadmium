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

import org.eclipse.jgit.util.StringUtils;
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
public class StateUpdateCommandAction implements CommandAction<StateUpdateRequest> {
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
  public boolean execute(CommandContext<StateUpdateRequest> ctx) throws Exception {
    StateUpdateRequest request = ctx.getMessage().getBody();
    try {
      if(!StringUtils.isEmptyOrNull(request.getState())) {
        UpdateState newState = UpdateState.valueOf(request.getState());
        if(newState != UpdateState.UPDATING || !lifecycleService.isMe(new ChannelMember(ctx.getSource())) || lifecycleService.getCurrentState() != UpdateState.WAITING) {
          lifecycleService.updateState(new ChannelMember(ctx.getSource()), newState);
        }
        if(lifecycleService.getCurrentState() == UpdateState.WAITING && lifecycleService.allEquals(UpdateState.WAITING)) {
          log.info("Done updating content now switching content.");
          maintFilter.start();
          fileServlet.switchContent(ctx.getMessage().getHeader().getRequestTime());
          if(processor != null) {
            processor.makeLive();
          }
          setHistoryDone(ctx);
          maintFilter.stop();
          lifecycleService.updateMyState(UpdateState.IDLE, request.getUuid());
        }
      } else if(!StringUtils.isEmptyOrNull(request.getConfigState())) {
        UpdateState newState = UpdateState.valueOf(request.getConfigState());
        if(newState != UpdateState.UPDATING || !lifecycleService.isMe(new ChannelMember(ctx.getSource())) || lifecycleService.getCurrentConfigState() != UpdateState.WAITING) {
          lifecycleService.updateConfigState(new ChannelMember(ctx.getSource()), newState);
        }
        if(lifecycleService.getCurrentConfigState() == UpdateState.WAITING && lifecycleService.allEqualsConfig(UpdateState.WAITING)) {
          log.info("Done updating config now switching config.");
          maintFilter.start();
          configManager.makeConfigParserLive();
          setHistoryDone(ctx);
          maintFilter.stop();
          lifecycleService.updateMyConfigState(UpdateState.IDLE, request.getUuid());
        }
      }
    } catch(Exception e) {
      log.warn("Failed to run state update command action", e);
      return false;
    }
    return true;
  }

  private void setHistoryDone(CommandContext<StateUpdateRequest> ctx) {
    StateUpdateRequest request = ctx.getMessage().getBody();
    if(!StringUtils.isEmptyOrNull(request.getUuid())) {
      historyManager.markHistoryEntryAsFinished(request.getUuid());
    }
  }

  @Override
  public void handleFailure(CommandContext<StateUpdateRequest> ctx, Exception e) {
    log.warn("Failed to update state");
  }

}
