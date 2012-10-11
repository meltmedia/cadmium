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
package com.meltmedia.cadmium.core.worker;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.ContentGitService;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.commands.ContentUpdateRequest;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;

@Singleton
public class CoordinatedWorkerImpl implements CoordinatedWorker<ContentUpdateRequest>, CoordinatedWorkerListener<ContentUpdateRequest>, Closeable {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private ExecutorService pool;
  
  @Inject
  @ContentGitService
  protected DelayedGitServiceInitializer service;

  @Inject
  protected ConfigManager configManager;
  
  @Inject
  @Named("contentDir")
  protected String contentDir;
  
  @Inject
  protected MessageSender sender;
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected SiteConfigProcessor processor;
  
  @Inject
  protected HistoryManager historyManager;
    
  protected Future<Boolean> lastTask = null;
  protected volatile String nextDirectory = null;  

  protected CoordinatedWorkerListener<ContentUpdateRequest> listener;
  protected Properties configProperties;
  
  public CoordinatedWorkerImpl() {
    pool = Executors.newSingleThreadExecutor();
    listener = this;
  }

  @Override
  public void beginPullUpdates(final ContentUpdateRequest body) {
    synchronized(pool) {
      log.info("Beginning Update...");
      lastTask = null;
      configProperties = configManager.getDefaultProperties();
      
      try {
        log.debug("Waiting for git service to initialize.");
        service.getGitService();
        service.releaseGitService();
        
        if(body.getContentLocation() != null && !StringUtils.isEmptyOrNull(body.getContentLocation().getRepository())) {
          lastTask = pool.submit(new SwitchRepositoryTask(service, body.getContentLocation().getRepository(), lastTask));
        }
        
        if(body.getContentLocation() != null && !StringUtils.isEmptyOrNull(body.getContentLocation().getRevision())) {
          configProperties.setProperty("updating.content.to.sha", body.getContentLocation().getRevision());
        }
        if(body.getContentLocation() != null && !StringUtils.isEmptyOrNull(body.getContentLocation().getBranch())) {
          configProperties.setProperty("updating.content.to.branch", body.getContentLocation().getBranch());
          lastTask = pool.submit(new SwitchBranchTask(service, body.getContentLocation().getBranch(), lastTask));
        }
        
        lastTask = pool.submit(new PullUpdateTask("content", service, configProperties, lastTask));
        
        if(body.getContentLocation() != null && !StringUtils.isEmptyOrNull(body.getContentLocation().getRevision())) {
          lastTask = pool.submit(new ResetToRevTask("content", service, body.getContentLocation().getRevision(), configProperties, lastTask));
        }
        
        String contentDir = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");
        if(StringUtils.isEmptyOrNull(contentDir)) {
          contentDir = this.contentDir;
        }
        
        lastTask = pool.submit(new CreateNewRenderedDirectoryTask(service, contentDir, body, lastTask) {
          @Override
          public void setNextDirectory(String nextDirectory) {
            CoordinatedWorkerImpl.this.nextDirectory = nextDirectory;
          }
        });
        
        lastTask = pool.submit(new UpdateMetaConfigsTask(processor, body, lastTask) {
          @Override
          public String getNextDirectory() {
            return CoordinatedWorkerImpl.this.nextDirectory;
          }
        });
        
        lastTask = pool.submit(new UpdateConfigTask(null, service, body, configManager, lastTask) {

          @Override
          public String getNextDirectory() {
            return CoordinatedWorkerImpl.this.nextDirectory;
          }
        });
        
        lastTask = pool.submit(new NotifyListenerTask(listener, body, lastTask));
        
        lastTask = pool.submit(new CleanUpTask("com.meltmedia.cadmium.lastUpdated", listener, configProperties, body, lastTask));
      } catch(Throwable t) {
        log.error("Failed to run update.", t);
        throw new Error(t);
      }
    }
  }

  @Override
  public void killUpdate() {
    IOUtils.closeQuietly(this);
    pool = Executors.newSingleThreadExecutor();
  }

  @Override
  public void setListener(CoordinatedWorkerListener<ContentUpdateRequest> listener) {
    this.listener = listener;
  }

  @Override
  public CoordinatedWorkerListener<ContentUpdateRequest> getListener() {
    return listener;
  }

  @Override
  public void workDone(ContentUpdateRequest body) {
    log.info("Work is done");
    lifecycleService.updateMyState(UpdateState.WAITING, null, false);
    Message<ContentUpdateRequest> doneMessage = new Message<ContentUpdateRequest>(ProtocolMessage.UPDATE_DONE, body);
    try { 
      sender.sendMessage(doneMessage, null);
    } catch (Exception e) {
      log.warn("Failed to send done message: {}", e.getMessage());
    }
  }

  @Override
  public void workFailed(ContentUpdateRequest body) {
    log.info("Work has failed");
    Message<ContentUpdateRequest> doneMessage = new Message<ContentUpdateRequest>(ProtocolMessage.UPDATE_FAILED, body);
    try {
      sender.sendMessage(doneMessage, null);
    } catch (Exception e) {
      log.warn("Failed to send fail message: {}", e.getMessage());
    }
  }

  @Override
  public void close() throws IOException {
    try {
      if(!pool.isShutdown() || !pool.isTerminated()) {
        pool.shutdownNow();
      }
    } catch(Throwable t) {
      throw new IOException(t);
    } finally {
      pool = null;
    }
  }

}
