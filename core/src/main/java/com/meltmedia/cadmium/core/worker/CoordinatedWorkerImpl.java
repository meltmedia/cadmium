package com.meltmedia.cadmium.core.worker;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;

@Singleton
public class CoordinatedWorkerImpl implements CoordinatedWorker, CoordinatedWorkerListener {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private ExecutorService pool;
  
  @Inject
  protected GitService service;

  @Inject
  @Named("config.properties")
  protected Properties configProperties;
  
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
  
  private CoordinatedWorkerListener listener;
  
  public CoordinatedWorkerImpl() {
    pool = Executors.newSingleThreadExecutor();
    listener = this;
  }

  @Override
  public void beginPullUpdates(final Map<String, String> properties) {
    synchronized(pool) {
      log.info("Beginning Update...");
      lastTask = null;
      try {
        if(properties.containsKey("sha")) {
          configProperties.setProperty("updating.to.sha", properties.get("sha"));
        }
        if(properties.containsKey("branch")) {
          configProperties.setProperty("updating.to.branch", properties.get("branch"));
          lastTask = pool.submit(new SwitchBranchTask(service, properties.get("branch"), lastTask));
        }
        
        lastTask = pool.submit(new PullUpdateTask(service, configProperties, lastTask));
        
        if(properties.containsKey("sha")) {
          lastTask = pool.submit(new ResetToRevTask(service, properties.get("sha"), configProperties, lastTask));
        }
        
        String contentDir = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");
        if(contentDir == null || contentDir.length() == 0) {
          contentDir = this.contentDir;
        }
        
        lastTask = pool.submit(new CreateNewRenderedDirectoryTask(service, contentDir, properties, lastTask));
        
        lastTask = pool.submit(new UpdateMetaConfigsTask(processor, properties, lastTask));
        
        lastTask = pool.submit(new UpdateConfigTask(service, properties, configProperties, historyManager, lastTask));
        
        lastTask = pool.submit(new NotifyListenerTask(listener, properties, lastTask));
        
        lastTask = pool.submit(new CleanUpTask(listener, configProperties, properties, lastTask));
      } catch(Throwable t) {
        log.error("Failed to run update.", t);
        throw new Error(t);
      }
    }
  }

  @Override
  public void killUpdate() {
    synchronized(pool) {
      pool.shutdownNow();
    }
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
  public void workDone() {
    log.info("Work is done");
    lifecycleService.updateMyState(UpdateState.WAITING, false);
    Message doneMessage = new Message();
    doneMessage.setCommand(ProtocolMessage.UPDATE_DONE);
    try {
      sender.sendMessage(doneMessage, null);
    } catch (Exception e) {
      log.warn("Failed to send done message: {}", e.getMessage());
    }
  }

  @Override
  public void workFailed(String branch, String sha, String openId) {
    log.info("Work has failed");
    Message doneMessage = new Message();
    doneMessage.setCommand(ProtocolMessage.UPDATE_FAILED);
    if(branch != null) {
      doneMessage.getProtocolParameters().put("branch", branch);
    }
    if(sha != null) {
      doneMessage.getProtocolParameters().put("sha", sha);
    }
    if(openId != null) {
      doneMessage.getProtocolParameters().put("openId", openId);
    }
    try {
      sender.sendMessage(doneMessage, null);
    } catch (Exception e) {
      log.warn("Failed to send fail message: {}", e.getMessage());
    }
  }

}
