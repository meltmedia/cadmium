package com.meltmedia.cadmium.core.worker;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.ConfigurationGitService;
import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class ConfigCoordinatedWorkerImpl implements CoordinatedWorker,
    CoordinatedWorkerListener, Closeable {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private ExecutorService pool;
  
  @Inject
  @ConfigurationGitService
  protected DelayedGitServiceInitializer service;

  @Inject
  protected ConfigManager configManager;
  
  @Inject
  protected MessageSender sender;
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  protected HistoryManager historyManager;
    
  protected Future<Boolean> lastTask = null;
  

  protected CoordinatedWorkerListener listener;
  protected Properties configProperties; 
  
  public ConfigCoordinatedWorkerImpl() {
    pool = Executors.newSingleThreadExecutor();
    listener = this;
  }

  @Override
  public void beginPullUpdates(final Map<String, String> properties) {
    synchronized(pool) {
      log.info("Beginning Config Update...");
      lastTask = null;
      configProperties = configManager.getDefaultProperties();
      
      try {
        log.debug("Waiting for git service to initialize.");
        service.getGitService();
        service.releaseGitService();
        
        if(properties.containsKey("repo") && properties.get("repo").trim().length() > 0) {
          lastTask = pool.submit(new SwitchRepositoryTask(service, properties.get("repo"), lastTask));
        }
        
        if(properties.containsKey("sha")) {
          configProperties.setProperty("updating.config.to.sha", properties.get("sha"));
        }
        if(properties.containsKey("branch")) {
          configProperties.setProperty("updating.config.to.branch", properties.get("branch"));
          lastTask = pool.submit(new SwitchBranchTask(service, properties.get("branch"), lastTask));
        }
        
        lastTask = pool.submit(new PullUpdateTask("config", service, configProperties, lastTask));
        
        if(properties.containsKey("sha")) {
          lastTask = pool.submit(new ResetToRevTask("config", service, properties.get("sha"), configProperties, lastTask));
        }
        
        String contentDir = configProperties.getProperty("com.meltmedia.cadmium.config.lastUpdated");
        
        lastTask = pool.submit(new CreateNewRenderedDirectoryTask(service, contentDir, properties, lastTask));
        
        lastTask = pool.submit(new ParseConfigDirectoryTask(listener, configManager, properties, lastTask));
        
        lastTask = pool.submit(new UpdateConfigTask("config", service, properties, configManager, lastTask));
        
        lastTask = pool.submit(new NotifyListenerTask(listener, properties, lastTask));
        
        lastTask = pool.submit(new CleanUpTask("com.meltmedia.cadmium.config.lastUpdated", listener, configProperties, properties, lastTask));
        
      } catch(Throwable t) {
        log.error("Failed to run config update.", t);
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
  public void setListener(CoordinatedWorkerListener listener) {
    this.listener = listener;
  }

  @Override
  public CoordinatedWorkerListener getListener() {
    return listener;
  }

  @Override
  public void workDone(Map<String, String> properties) {
    log.info("Config update work is done");
    lifecycleService.updateMyConfigState(UpdateState.WAITING, null, false);
    Message doneMessage = new Message();
    doneMessage.setCommand(ProtocolMessage.CONFIG_UPDATE_DONE);
    if(properties != null) {
      doneMessage.setProtocolParameters(properties);
    }
    try { 
      sender.sendMessage(doneMessage, null);
    } catch (Exception e) {
      log.warn("Failed to send done message: {}", e.getMessage());
    }
  }

  @Override
  public void workFailed(String repo, String branch, String sha, String openId, String uuid) {
    log.info("Config update work has failed");
    Message doneMessage = new Message();
    doneMessage.setCommand(ProtocolMessage.CONFIG_UPDATE_FAILED);
    if(repo != null) {
      doneMessage.getProtocolParameters().put("repo", repo);
    }
    if(branch != null) {
      doneMessage.getProtocolParameters().put("branch", branch);
    }
    if(sha != null) {
      doneMessage.getProtocolParameters().put("sha", sha);
    }
    if(openId != null) {
      doneMessage.getProtocolParameters().put("openId", openId);
    }
    if(uuid != null) {
      doneMessage.getProtocolParameters().put("uuid", uuid);
    }
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
