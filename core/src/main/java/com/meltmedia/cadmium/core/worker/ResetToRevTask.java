package com.meltmedia.cadmium.core.worker;

import java.util.Properties;
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
  private Properties configProperties;
  
  public ResetToRevTask(GitService service, String revision, Properties configProperties, Future<Boolean> previousTask) {
    this.service = service;
    this.revision = revision;
    this.previousTask = previousTask;
    this.configProperties = configProperties;
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
    configProperties.setProperty("updating.to.sha", revision);
    configProperties.setProperty("updating.to.branch", service.getBranchName());
    service.resetToRev(revision);
    return true;
  }

}
