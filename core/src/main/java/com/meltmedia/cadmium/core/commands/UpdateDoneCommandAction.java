/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.commands;

import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class UpdateDoneCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected HistoryManager manager;
  
  @Inject
  protected ConfigManager configManager;

  public String getName() { return ProtocolMessage.UPDATE_DONE; }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    
    Properties configProperties = configManager.getDefaultProperties();
    
    log.info("Update is done @ {}, my state {}", ctx.getSource(), lifecycleService.getCurrentState());
    if(manager != null) {
      try {
        Map<String, String> props = ctx.getMessage().getProtocolParameters();
        String branch = props.get("BranchName");
        String rev = props.get("CurrentRevision");
        String openId = props.get("openId");
        String lastUpdated = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");
        String uuid = props.get("uuid");
        String comment = props.get("comment");
        boolean revertible = !new Boolean(props.get("nonRevertible"));
        manager.logEvent(branch, rev, openId, lastUpdated, uuid, comment, revertible, false);
      } catch(Exception e){
        log.warn("Failed to update log", e);
      }
    }
    lifecycleService.sendStateUpdate(null, ctx.getMessage().getProtocolParameters().get("uuid"));
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }
}
