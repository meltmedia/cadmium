package com.meltmedia.cadmium.core.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.git.GitService;

public class SwitchBranchTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private GitService service;
  private String branch;
  private Future<Boolean> previousTask;
  
  public SwitchBranchTask(GitService service, String branchName, Future<Boolean> previousTask) {
    this.service = service;
    this.branch = branchName;
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
    service.fetchRemotes();
    if(service.isBranch(branch) || service.isTag(branch)) {
      log.info("Switching branch to {}",branch);
      service.switchBranch(branch);
    } else {
      throw new Exception("The branch ["+branch+"] does not exist");
    }
    return true;
  }

}
