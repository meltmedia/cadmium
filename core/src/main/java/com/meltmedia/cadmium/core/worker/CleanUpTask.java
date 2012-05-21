package com.meltmedia.cadmium.core.worker;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.FileSystemManager;

public class CleanUpTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private Properties configProperties;
  private Future<Boolean> previousTask;
  private CoordinatedWorkerListener listener;
  
  public CleanUpTask(CoordinatedWorkerListener listener, Properties configProperties, Future<Boolean> previousTask) {
    this.configProperties = configProperties;
    this.previousTask = previousTask;
    this.listener = listener;
  }

  @Override
  public Boolean call() throws Exception {
    if(previousTask != null) {
      try{
        Boolean lastResponse = previousTask.get();
        if(lastResponse != null && !lastResponse.booleanValue() ) {
          throw new Exception("Previous task failed");
        }
      } catch(Exception e) {
        listener.workFailed();
        return false;
      }
    }
    
    log.info("Cleaning up content directories");
    
    FileSystemManager.cleanUpOld(configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"), 1);
    
    return true;
  }

}
