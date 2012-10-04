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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.ConfigurationGitService;
import com.meltmedia.cadmium.core.ConfigurationWorker;
import com.meltmedia.cadmium.core.ContentGitService;
import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.ContentWorker;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.history.HistoryEntry.EntryType;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
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
  @ContentWorker
  protected CoordinatedWorker worker;
  
  @Inject
  @ConfigurationWorker
  protected CoordinatedWorker configWorker;
  
  @Inject
  protected MembershipTracker tracker;
  
  @Inject
  protected ConfigManager configManager;
  
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
  @ContentGitService
  protected DelayedGitServiceInitializer gitInit;
  
  @Inject
  @ConfigurationGitService
  protected DelayedGitServiceInitializer gitConfigInit;
  
  protected Properties configProperties;

  
  public String getName() { return ProtocolMessage.SYNC; }
  
  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    
    configProperties = configManager.getDefaultProperties();
    
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
    boolean updateConfig = false;
    if(ctx.getMessage().getProtocolParameters().containsKey("repo") || ctx.getMessage().getProtocolParameters().containsKey("branch") || ctx.getMessage().getProtocolParameters().containsKey("sha")) {
      update = true;
    }
    if(ctx.getMessage().getProtocolParameters().containsKey("configRepo") || ctx.getMessage().getProtocolParameters().containsKey("configBranch") || ctx.getMessage().getProtocolParameters().containsKey("configSha")) {
      updateConfig = true;
    }   
    ctx.getMessage().getProtocolParameters().put("comment", "SYNC");
    if(update) {
      performContentUpdate(ctx);
    }
    
    if(updateConfig) {
      performConfigUpdate(ctx);
    }
  }
  
  private void performConfigUpdate(final CommandContext ctx) {
    log.info("Begining Configuration sync update!");
    final CoordinatedWorkerListener oldListener = configWorker.getListener();
    configWorker.setListener(new CoordinatedWorkerListener(){

      @Override
      public void workDone(Map<String, String> properties) {
        log.info("Config Sync done");
        configManager.makeConfigParserLive();
        try {
          String repo = properties.get("repo");
          String branch = properties.get("BranchName");
          String rev = properties.get("CurrentRevision");
          String lastUpdated = configProperties.getProperty("com.meltmedia.cadmium.config.lastUpdated");
          String comment = properties.get("comment");
          manager.logEvent(EntryType.CONFIG, repo, branch, rev, "AUTO", lastUpdated, null, comment, true, true);
        } catch(Exception e){
          log.warn("Failed to update log", e);
        }
        configWorker.setListener(oldListener);
      }

      @Override
      public void workFailed(String repo, String branch, String sha,
          String openId, String uuid) {
        log.info("Config Sync failed");
        
        configWorker.setListener(oldListener);
      }
    });
    Map<String, String> properties = new HashMap<String, String> ();
    properties.putAll(ctx.getMessage().getProtocolParameters());
    properties.put("repo", properties.get("configRepo"));
    properties.put("branch", properties.get("configBranch"));
    properties.put("sha", properties.get("configSha"));
    configWorker.beginPullUpdates(properties);
  }
 
  private void performContentUpdate(final CommandContext ctx) {
    log.info("Taking site down to run sync update for content!");
    maintFilter.start();
    final CoordinatedWorkerListener oldListener = worker.getListener();
    worker.setListener(new CoordinatedWorkerListener() {
      
     @Override
      public void workDone(Map<String, String> properties) {
        log.info("Content Sync done");
        fileServlet.switchContent(ctx.getMessage().getRequestTime());
        processor.makeLive();
        try {
          String repo = properties.get("repo");
          String branch = properties.get("BranchName");
          String rev = properties.get("CurrentRevision");
          String lastUpdated = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");
          String comment = properties.get("comment");
          manager.logEvent(EntryType.CONTENT, repo, branch, rev, "AUTO", lastUpdated, null, comment, true, true);
        } catch(Exception e){
          log.warn("Failed to update log", e);
        }
        maintFilter.stop();
        worker.setListener(oldListener);
      }

      @Override
      public void workFailed(String repo, String branch, String sha, String openId, String uuid) {
        log.info("Content Sync failed");
        worker.setListener(oldListener);
      }
    });
    
    this.worker.beginPullUpdates(ctx.getMessage().getProtocolParameters());
  }

  private void handleCommandAsCoordinator(CommandContext ctx) {
    log.info("Received SYNC message from new member {}", ctx.getSource());
    if(gitInit != null) {
      try {
        log.info("Waiting for content git service to initialize.");
        gitInit.getGitService();
        gitInit.releaseGitService();
      } catch(Exception e){
        log.warn("Waiting was interrupted.", e);
        return;
      }
    }
    if(gitConfigInit != null) {
      try {
        log.info("Waiting for config git service to initialize.");
        gitConfigInit.getGitService();
        gitConfigInit.releaseGitService();
      } catch(Exception e){
        log.warn("Waiting was interrupted.", e);
        return;
      }
    }
    boolean update = false;
    boolean updateConfig = false;
    if(ctx.getMessage().getProtocolParameters().containsKey("repo") && ctx.getMessage().getProtocolParameters().containsKey("branch") && ctx.getMessage().getProtocolParameters().containsKey("sha")) {
      log.info("Sync Content Request has repo {} and branch {} and sha {}", new Object[] {ctx.getMessage().getProtocolParameters().get("repo"), ctx.getMessage().getProtocolParameters().get("branch"), ctx.getMessage().getProtocolParameters().get("sha")});
      if(configProperties.containsKey("repo") && configProperties.containsKey("branch") && configProperties.containsKey("git.ref.sha")) {
        log.info("I have content repo {} and branch {} and sha {}", new Object[] {configProperties.get("repo"), configProperties.get("branch"), configProperties.get("git.ref.sha")});
        if(!configProperties.getProperty("repo").equals(ctx.getMessage().getProtocolParameters().get("repo")) || !configProperties.getProperty("branch").equals(ctx.getMessage().getProtocolParameters().get("branch")) || !configProperties.getProperty("git.ref.sha").equals(ctx.getMessage().getProtocolParameters().get("sha"))) {
          log.info("Content update is required!");
          update = true;
        }
      }
    } else if (configProperties.containsKey("repo") && configProperties.containsKey("branch") && configProperties.containsKey("git.ref.sha")) {
      log.info("Sync content request has no repo and/or branch and/or sha! update is required!");
      update = true;
    }
    if(ctx.getMessage().getProtocolParameters().containsKey("configRepo") && ctx.getMessage().getProtocolParameters().containsKey("configBranch") && ctx.getMessage().getProtocolParameters().containsKey("configSha")) {
      log.info("Sync Config Request has repo {} and branch {} and sha {}", new Object[] {ctx.getMessage().getProtocolParameters().get("configRepo"), ctx.getMessage().getProtocolParameters().get("configBranch"), ctx.getMessage().getProtocolParameters().get("configSha")});
      if(configProperties.containsKey("config.repo") && configProperties.containsKey("config.branch") && configProperties.containsKey("config.git.ref.sha")) {
        log.info("I have config repo {} and branch {} and sha {}", new Object[] {configProperties.get("config.repo"), configProperties.get("config.branch"), configProperties.get("config.git.ref.sha")});
        if(!configProperties.getProperty("config.repo").equals(ctx.getMessage().getProtocolParameters().get("configRepo")) || !configProperties.getProperty("config.branch").equals(ctx.getMessage().getProtocolParameters().get("configBranch")) || !configProperties.getProperty("config.git.ref.sha").equals(ctx.getMessage().getProtocolParameters().get("configSha"))) {
          log.info("Config update is required!");
          updateConfig = true;
        }
      }
    } else if (configProperties.containsKey("config.repo") && configProperties.containsKey("config.branch") && configProperties.containsKey("config.git.ref.sha")) {
      log.info("Sync config request has no repo and/or branch and/or sha! update is required!");
      updateConfig = true;
    }
    
    if(update || updateConfig) {
      Message syncMessage = new Message();
      syncMessage.setCommand(getName());
      if(update) {
        syncMessage.getProtocolParameters().put("repo", configProperties.getProperty("repo"));
        syncMessage.getProtocolParameters().put("branch", configProperties.getProperty("branch"));
        syncMessage.getProtocolParameters().put("sha", configProperties.getProperty("git.ref.sha"));
        log.info("Sending content SYNC message to new member {}, repo {}, branch {}, sha {}", new Object[] {ctx.getSource(), configProperties.getProperty("repo"), configProperties.getProperty("branch"), configProperties.getProperty("git.ref.sha")});
      }
      if(updateConfig) {
        syncMessage.getProtocolParameters().put("configRepo", configProperties.getProperty("config.repo"));
        syncMessage.getProtocolParameters().put("configBranch", configProperties.getProperty("config.branch"));
        syncMessage.getProtocolParameters().put("configSha", configProperties.getProperty("config.git.ref.sha"));
        log.info("Sending config SYNC message to new member {}, repo {}, branch {}, sha {}", new Object[] {ctx.getSource(), configProperties.getProperty("config.repo"), configProperties.getProperty("config.branch"), configProperties.getProperty("config.git.ref.sha")});
      }
      try{
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
