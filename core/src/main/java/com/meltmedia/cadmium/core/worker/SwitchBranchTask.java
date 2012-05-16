package com.meltmedia.cadmium.core.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.meltmedia.cadmium.core.git.GitService;

public class SwitchBranchTask implements Callable<Boolean> {
  
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
    service.switchBranch(branch);
    return true;
  }

}
