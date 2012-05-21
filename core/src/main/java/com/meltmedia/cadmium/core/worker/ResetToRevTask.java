package com.meltmedia.cadmium.core.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.git.GitService;

public class ResetToRevTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private GitService service;
  private String revision;
  private Future<Boolean> previousTask;
  
  public ResetToRevTask(GitService service, String revision, Future<Boolean> previousTask) {
    this.service = service;
    this.revision = revision;
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
    log.info("Resetting to revision {}", revision);
    service.resetToRev(revision);
    return true;
  }

}
