package com.meltmedia.cadmium.core.worker;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CoordinatedWorkerListener;

public class NotifyListenerTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private CoordinatedWorkerListener listener;
  private Future<Boolean> previousTask;
  private Map<String, String> properties;
  
  public NotifyListenerTask(CoordinatedWorkerListener listener, Map<String, String> properties, Future<Boolean> previousTask) {
    this.listener = listener;
    this.previousTask = previousTask;
    this.properties = properties;
  }

  @Override
  public Boolean call() throws Exception {
    if(previousTask != null) {
      Boolean lastResponse = previousTask.get();
      if(lastResponse != null && !lastResponse.booleanValue() ) {
        log.warn("Notification of work failed!");
        listener.workFailed(properties.get("branch"), properties.get("sha"), properties.get("openId"));
        throw new Exception("Previous task failed");
      }
    }
    
    Thread.sleep(1000l);
    log.info("Notifying listener of updating being done successfully");
    listener.workDone();
    
    return true;
  }

}
