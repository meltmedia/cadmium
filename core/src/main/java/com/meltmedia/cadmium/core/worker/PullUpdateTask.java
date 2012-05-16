package com.meltmedia.cadmium.core.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.meltmedia.cadmium.core.git.GitService;

public class PullUpdateTask implements Callable<Boolean> {
  
  private GitService service;
  private Future<Boolean> previousTask;
  
  public PullUpdateTask(GitService service, Future<Boolean> previousTask) {
    this.service = service;
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
    
    return service.pull();
  }

}
