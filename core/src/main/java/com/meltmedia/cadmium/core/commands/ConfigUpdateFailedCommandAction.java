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
import com.meltmedia.cadmium.core.ConfigurationWorker;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.history.HistoryEntry.EntryType;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class ConfigUpdateFailedCommandAction implements CommandAction<ContentUpdateRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  public static final String FAILED_LOG_MESSAGE = "Config update failed to run!";
  
  @Inject
  @ConfigurationWorker
  protected CoordinatedWorker<ContentUpdateRequest> worker;
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected HistoryManager historyManager;

  public String getName() { return ProtocolMessage.CONFIG_UPDATE_FAILED; }

  @Override
  public boolean execute(CommandContext<ContentUpdateRequest> ctx) throws Exception {
    if(lifecycleService.getCurrentConfigState() != UpdateState.IDLE) {
      log.info("config update has failed @ {}", ctx.getSource());
      worker.killUpdate();
      lifecycleService.updateMyConfigState(UpdateState.IDLE);
      if(historyManager != null) {
        ContentUpdateRequest body = ctx.getMessage().getBody();
        historyManager.logEvent(EntryType.CONFIG, 
            body.getContentLocation(), 
            emptyStringIfNull(body.getOpenId()),
            "",
            body.getUuid(),
            FAILED_LOG_MESSAGE,
            false,
            false,
            true,
            true);
      }
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext<ContentUpdateRequest> ctx, Exception e) {

  }
  
  private static String emptyStringIfNull( final String value ) {
    if( value == null ) return "";
    else return value;
  }

}
