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
import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;

@Singleton
public class SyncCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected CoordinatedWorker worker;
  
  @Inject
  protected MembershipTracker tracker;
  
  @Inject
  @Named("config.properties")
  protected Properties configProperties;
  
  @Inject
  protected MessageSender sender;
  
  @Inject
  protected SiteDownService maintFilter;
  
  @Inject
  protected ContentService fileServlet;
  
  @Inject
  protected HistoryManager manager;

  @Inject
  protected SiteConfigProcessor processor;
  
  @Inject
  protected DelayedGitServiceInitializer gitInit;
  
  private GitService git;

  public String getName() { return ProtocolMessage.SYNC; }
  
  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    if(!tracker.getCoordinator().isMine()) {
      handleCommandAsNonCoordinator(ctx);
    } else {
      handleCommandAsCoordinator(ctx);
    }
    return true;
  }

  private void handleCommandAsNonCoordinator(final CommandContext ctx) {
    log.info("Received SYNC request from coordinator");
    boolean update = false;
    if(ctx.getMessage().getProtocolParameters().containsKey("branch") || ctx.getMessage().getProtocolParameters().containsKey("sha")) {
      update = true;
    }
    
    if(update) {
      log.info("Taking site down to run sync update!");
      maintFilter.start();
      final CoordinatedWorkerListener oldListener = worker.getListener();
      worker.setListener(new CoordinatedWorkerListener() {
        
       @Override
        public void workDone(Map<String, String> properties) {
          log.info("Sync done");
          fileServlet.switchContent(ctx.getMessage().getRequestTime());
          if(processor != null) {
            processor.makeLive();
          }
          if(manager != null) {
            try {
              manager.logEvent(properties.get("BranchName"), properties.get("CurrentRevision"), "SYNC".equals(properties.get("comment")) ? "AUTO" : properties.get("openId"), configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"), properties.get("uuid"), properties.get("comment"), !new Boolean(properties.get("nonRevertible")), true);
            } catch(Exception e){
              log.warn("Failed to update log", e);
            }
          }
          maintFilter.stop();
          worker.setListener(oldListener);
        }

        @Override
        public void workFailed(String branch, String sha, String openId, String uuid) {
          log.info("Sync failed");
          worker.setListener(oldListener);
        }
      });
      ctx.getMessage().getProtocolParameters().put("comment", "SYNC");
      this.worker.beginPullUpdates(ctx.getMessage().getProtocolParameters());
    }
  }

  private void handleCommandAsCoordinator(CommandContext ctx) {
    log.info("Received SYNC message from new member {}", ctx.getSource());
    if(git != null && gitInit != null) {
      try {
        log.info("Waiting for git service to initialize.");
        git = gitInit.getGitService();
      } catch(Exception e){
        log.warn("Waiting was interrupted.", e);
        return;
      }
    }
    boolean update = false;
    if(ctx.getMessage().getProtocolParameters().containsKey("branch") && ctx.getMessage().getProtocolParameters().containsKey("sha")) {
      log.info("Sync Request has branch {} and sha {}", ctx.getMessage().getProtocolParameters().get("branch"), ctx.getMessage().getProtocolParameters().get("sha"));
      if(configProperties.containsKey("branch") && configProperties.containsKey("git.ref.sha")) {
        log.info("I have branch {} and sha {}", configProperties.get("branch"), configProperties.get("git.ref.sha"));
        if(!configProperties.getProperty("branch").equals(ctx.getMessage().getProtocolParameters().get("branch")) || !configProperties.getProperty("git.ref.sha").equals(ctx.getMessage().getProtocolParameters().get("sha"))) {
          log.info("Update is required!");
          update = true;
        }
      }
    } else if (configProperties.containsKey("branch") && configProperties.containsKey("git.ref.sha")) {
      log.info("Sync request has no branch and/or sha! update is required!");
      update = true;
    }
    
    if(update) {
      Message syncMessage = new Message();
      syncMessage.setCommand(getName());
      syncMessage.getProtocolParameters().put("branch", configProperties.getProperty("branch"));
      syncMessage.getProtocolParameters().put("sha", configProperties.getProperty("git.ref.sha"));
      try{
        log.info("Sending SYNC message to new member {}, branch {}, sha {}", new Object[] {ctx.getSource(), configProperties.getProperty("branch"), configProperties.getProperty("git.ref.sha")});
        sender.sendMessage(syncMessage, new ChannelMember(ctx.getSource()));
      } catch(Exception e) {
        log.warn("Failed to send SYNC message", e);
      }
    }
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {
    
  }

}
