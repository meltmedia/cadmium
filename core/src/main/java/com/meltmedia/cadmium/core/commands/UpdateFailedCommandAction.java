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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class UpdateFailedCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  public static final String FAILED_LOG_MESSAGE = "Update failed to run!";
  
  @Inject
  protected CoordinatedWorker worker;
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected HistoryManager historyManager;

  public String getName() { return ProtocolMessage.UPDATE_FAILED; }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    if(lifecycleService.getCurrentState() != UpdateState.IDLE) {
      log.info("update has failed @ {}", ctx.getSource());
      worker.killUpdate();
      lifecycleService.updateMyState(UpdateState.IDLE);
      if(historyManager != null) {
        String repo = "";
        if(ctx.getMessage().getProtocolParameters().containsKey("repo")) {
          repo = ctx.getMessage().getProtocolParameters().get("repo");
        }
        String branch = "";
        if(ctx.getMessage().getProtocolParameters().containsKey("branch")) {
          branch = ctx.getMessage().getProtocolParameters().get("branch");
        }
        String sha = "";
        if(ctx.getMessage().getProtocolParameters().containsKey("sha")) {
          sha = ctx.getMessage().getProtocolParameters().get("sha");
        }
        String openId = "";
        if(ctx.getMessage().getProtocolParameters().containsKey("openId")) {
          openId = ctx.getMessage().getProtocolParameters().get("openId");
        }
        historyManager.logEvent(repo, branch, sha, openId, "", ctx.getMessage().getProtocolParameters().get("uuid"), FAILED_LOG_MESSAGE, false, false, true, true);
      }
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }

}
