package com.meltmedia.cadmium.core.worker;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.git.GitService;

public class PullUpdateTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private GitService service;
  private Future<Boolean> previousTask;
  private Properties configProperties;
  
  public PullUpdateTask(GitService service, Properties configProperties, Future<Boolean> previousTask) {
    this.service = service;
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
    if(!service.isTag(service.getBranchName())) {
      log.info("Pulling latest Github updates.");
      boolean retVal = service.pull();
      configProperties.setProperty("updating.to.sha", service.getCurrentRevision());
      configProperties.setProperty("updating.to.branch", service.getBranchName());
      return retVal;
    } else {
      configProperties.setProperty("updating.to.sha", service.getCurrentRevision());
      configProperties.setProperty("updating.to.branch", service.getBranchName());
      log.info("Skipping pull since tags cannot update.");
      return true;
    }
  }

}
