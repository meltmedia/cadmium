package com.meltmedia.cadmium.core.worker;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.meltmedia.cadmium.core.FileSystemManager;

public class CleanUpTask implements Callable<Boolean> {
  private Properties configProperties;
  private Future<Boolean> previousTask;
  
  public CleanUpTask(Properties configProperties, Future<Boolean> previousTask) {
    this.configProperties = configProperties;
    this.previousTask = previousTask;
  }

  @Override
  public Boolean call() throws Exception {
    if(previousTask != null) {
      Boolean lastResponse = previousTask.get();
      if(lastResponse != null && !lastResponse.booleanValue() ) {
        throw new Exception("Previous task failed");
      }
    }
    
    FileSystemManager.cleanUpOld(configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"), 1);
    
    return true;
  }

}
