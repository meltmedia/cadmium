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
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

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
  protected MessageSender sender;
  
  protected Future<Boolean> lastTask = null;
  
  private CoordinatedWorkerListener listener;
  
  public CoordinatedWorkerImpl() {
    pool = Executors.newSingleThreadExecutor();
    listener = this;
  }

  @Override
  public void beginPullUpdates(Map<String, String> properties) {
    synchronized(pool) {
      lastTask = null;
      
      if(properties.containsKey("branch")) {
        lastTask = pool.submit(new SwitchBranchTask(service, properties.get("branch"), lastTask));
      }
      
      lastTask = pool.submit(new PullUpdateTask(service, lastTask));
      
      if(properties.containsKey("sha")) {
        lastTask = pool.submit(new ResetToRevTask(service, properties.get("sha"), lastTask));
      }
      
      lastTask = pool.submit(new CreateNewRenderedDirectoryTask(service, configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"), properties, lastTask));
      
      lastTask = pool.submit(new UpdateConfigTask(service, properties, configProperties, lastTask));
      
      lastTask = pool.submit(new NotifyListenerTask(listener, lastTask));
      
      lastTask = pool.submit(new CleanUpTask(configProperties, lastTask));
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
    Message doneMessage = new Message();
    doneMessage.setCommand(ProtocolMessage.UPDATE_DONE);
    try {
      sender.sendMessage(doneMessage, null);
    } catch (Exception e) {
      log.warn("Failed to send done message: {}", e.getMessage());
    }
  }

  @Override
  public void workFailed() {
    log.info("Work has failed");
    Message doneMessage = new Message();
    doneMessage.setCommand(ProtocolMessage.UPDATE_FAILED);
    try {
      sender.sendMessage(doneMessage, null);
    } catch (Exception e) {
      log.warn("Failed to send fail message: {}", e.getMessage());
    }
  }

}
