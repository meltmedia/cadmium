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

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.util.StringUtils;
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
public class SyncCommandAction implements CommandAction<SyncRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  @ContentWorker
  protected CoordinatedWorker<ContentUpdateRequest> worker;
  
  @Inject
  @ConfigurationWorker
  protected CoordinatedWorker<ContentUpdateRequest> configWorker;
  
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
  public boolean execute(CommandContext<SyncRequest> ctx) throws Exception {
    
    configProperties = configManager.getDefaultProperties();
    
    if(!tracker.getCoordinator().isMine()) {
      handleCommandAsNonCoordinator(ctx);
    } else {
      handleCommandAsCoordinator(ctx);
    }
    return true;
  }

  private void handleCommandAsNonCoordinator(final CommandContext<SyncRequest> ctx) {
    log.info("Received SYNC request from coordinator");
    boolean update = !isEmptyOrNull(ctx.getMessage().getBody().getContentLocation());
    boolean updateConfig = !isEmptyOrNull(ctx.getMessage().getBody().getConfigLocation());
    ctx.getMessage().getBody().setComment("SYNC");
    if(update) {
      performContentUpdate(ctx);
    }
    
    if(updateConfig) {
      performConfigUpdate(ctx);
    }
  }
  
  private void performConfigUpdate(final CommandContext<SyncRequest> ctx) {
    log.info("Begining Configuration sync update!");
    final CoordinatedWorkerListener<ContentUpdateRequest> oldListener = configWorker.getListener();
    configWorker.setListener(new CoordinatedWorkerListener<ContentUpdateRequest>(){

      @Override
      public void workDone(ContentUpdateRequest body) {
        log.info("Config Sync done");
        configManager.makeConfigParserLive();
        try {
          String lastUpdated = configProperties.getProperty("com.meltmedia.cadmium.config.lastUpdated");
          String comment = body.getComment();
          manager.logEvent(EntryType.CONFIG, body.getContentLocation(), "AUTO", lastUpdated, null, comment, true, true);
        } catch(Exception e){
          log.warn("Failed to update log", e);
        }
        configWorker.setListener(oldListener);
      }

      @Override
      public void workFailed(ContentUpdateRequest body) {
        log.info("Config Sync failed");
        
        configWorker.setListener(oldListener);
      }
    });
    SyncRequest request = ctx.getMessage().getBody();
    ContentUpdateRequest body = new ContentUpdateRequest();
    body.setContentLocation(new GitLocation(request.getConfigLocation()));
    body.setComment(request.getComment());
    configWorker.beginPullUpdates(body);
  }
 
  private void performContentUpdate(final CommandContext<SyncRequest> ctx) {
    log.info("Taking site down to run sync update for content!");
    maintFilter.start();
    final CoordinatedWorkerListener<ContentUpdateRequest> oldListener = worker.getListener();
    worker.setListener(new CoordinatedWorkerListener<ContentUpdateRequest>() {
      
     @Override
      public void workDone(ContentUpdateRequest body) {
        log.info("Content Sync done");
        fileServlet.switchContent(ctx.getMessage().getHeader().getRequestTime());
        processor.makeLive();
        try {
          String lastUpdated = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");
          String comment = body.getComment();
          manager.logEvent(EntryType.CONTENT, body.getContentLocation(), "AUTO", lastUpdated, null, comment, true, true);
        } catch(Exception e){
          log.warn("Failed to update log", e);
        }
        maintFilter.stop();
        worker.setListener(oldListener);
      }

      @Override
      public void workFailed(ContentUpdateRequest body) {
        log.info("Content Sync failed");
        worker.setListener(oldListener);
      }
    });
    
    SyncRequest request = ctx.getMessage().getBody();
    ContentUpdateRequest body = new ContentUpdateRequest();
    body.setContentLocation(new GitLocation(request.getContentLocation()));
    body.setComment(request.getComment());
    
    this.worker.beginPullUpdates(body);
  }

  private void handleCommandAsCoordinator(CommandContext<SyncRequest> ctx) {
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
    SyncRequest request = ctx.getMessage().getBody();
    boolean update = false;
    boolean updateConfig = false;
    // NOTE: There must be a way to reduce this block down to a couple of lines.  The properties objects are
    // causing the code to be like this.
    if(isComplete(request.getContentLocation())) {
      log.info("Sync Content Request {}", request);
      if(configProperties.containsKey("repo") &&
         configProperties.containsKey("branch") && 
         configProperties.containsKey("git.ref.sha")) {
        log.info("I have content repo {} and branch {} and sha {}", new Object[] {
            configProperties.get("repo"),
            configProperties.get("branch"),
            configProperties.get("git.ref.sha")});
        if(!configProperties.getProperty("repo").equals(request.getContentLocation().getRepository()) || 
           !configProperties.getProperty("branch").equals(request.getContentLocation().getBranch()) || 
           !configProperties.getProperty("git.ref.sha").equals(request.getContentLocation().getRevision())) {
          log.info("Content update is required!");
          update = true;
        }
      }
    } else if (configProperties.containsKey("repo") && 
        configProperties.containsKey("branch") && 
        configProperties.containsKey("git.ref.sha")) {
      log.info("Sync content request has no repo and/or branch and/or sha! update is required!");
      update = true;
    }
    if(isComplete(request.getConfigLocation())) {
      log.info("Sync Config Request has {}", request);
      if(configProperties.containsKey("config.repo") && 
          configProperties.containsKey("config.branch") && 
          configProperties.containsKey("config.git.ref.sha")) {
        log.info("I have config repo {} and branch {} and sha {}", new Object[] {
            configProperties.get("config.repo"), 
            configProperties.get("config.branch"), 
            configProperties.get("config.git.ref.sha")});
        if(!configProperties.getProperty("config.repo").equals(request.getConfigLocation().getRepository()) || 
            !configProperties.getProperty("config.branch").equals(request.getConfigLocation().getBranch()) || 
            !configProperties.getProperty("config.git.ref.sha").equals(request.getConfigLocation().getRevision())) {
          log.info("Config update is required!");
          updateConfig = true;
        }
      }
    } else if (configProperties.containsKey("config.repo") &&
        configProperties.containsKey("config.branch") &&
        configProperties.containsKey("config.git.ref.sha")) {
      log.info("Sync config request has no repo and/or branch and/or sha! update is required!");
      updateConfig = true;
    }
    
    if(update || updateConfig) {
      SyncRequest newRequest = new SyncRequest();
      if(update) {
        newRequest.setContentLocation(new GitLocation(
          configProperties.getProperty("repo"),
          configProperties.getProperty("branch"),
          configProperties.getProperty("git.ref.sha")));
        log.info("Sending content SYNC message to new member {}, repo {}, branch {}, sha {}", new Object[] {ctx.getSource(), configProperties.getProperty("repo"), configProperties.getProperty("branch"), configProperties.getProperty("git.ref.sha")});
      }
      if(updateConfig) {
        newRequest.setConfigLocation(new GitLocation(
          configProperties.getProperty("config.repo"),
          configProperties.getProperty("config.branch"),
          configProperties.getProperty("config.git.ref.sha")));
        log.info("Sending config SYNC message to new member {}, repo {}, branch {}, sha {}", new Object[] {ctx.getSource(), configProperties.getProperty("config.repo"), configProperties.getProperty("config.branch"), configProperties.getProperty("config.git.ref.sha")});
      }
      Message<SyncRequest> syncMessage = new Message<SyncRequest>(getName(), newRequest);
      try{
        sender.sendMessage(syncMessage, new ChannelMember(ctx.getSource()));
      } catch(Exception e) {
        log.warn("Failed to send SYNC message", e);
      }
    }
  }

  @Override
  public void handleFailure(CommandContext<SyncRequest> ctx, Exception e) {
    
  }
  
  /**
   * Returns true if a git location object is null or all of its values are
   * empty or null.
   * 
   * @param location the location object to test.
   * @return true if the git location object is null or all of its values are empty or null, false otherwise.
   */
  private static boolean isEmptyOrNull( GitLocation location ) {
    return location == null ||
        (StringUtils.isEmptyOrNull(location.getRepository()) &&
        StringUtils.isEmptyOrNull(location.getBranch()) &&
        StringUtils.isEmptyOrNull(location.getRevision()));
  }
  
  /**
   * Returns true if the git location is not null and all of its components are not empty or null.
   * 
   * @param location the location to test.
   * @return true if the location is not null and all of its components are not empty or null, false otherwise.
   */
  private static boolean isComplete( GitLocation location ) {
    return location != null &&
        !StringUtils.isEmptyOrNull(location.getRepository()) &&
        !StringUtils.isEmptyOrNull(location.getBranch()) &&
        !StringUtils.isEmptyOrNull(location.getRevision());
        
  }

}
