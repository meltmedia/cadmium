package com.meltmedia.cadmium.core.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;

public class SwitchRepositoryTask implements Callable<Boolean> {
  
  private DelayedGitServiceInitializer service;
  private String repository;
  private Future<Boolean> lastTask;
  
  public SwitchRepositoryTask(DelayedGitServiceInitializer service, String repository, Future<Boolean> lastTask) {
    this.service = service;
    this.repository = repository;
    this.lastTask = lastTask;
  }

  @Override
  public Boolean call() throws Exception {
    if(lastTask != null) {
      Boolean lastResponse = lastTask.get();
      if(lastResponse != null && !lastResponse.booleanValue() ) {
        throw new Exception("Previous task failed");
      }
    }
    service.switchRepository(repository);
    return true;
  }

}
