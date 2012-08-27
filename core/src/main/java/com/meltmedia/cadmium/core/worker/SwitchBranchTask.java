/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
import com.meltmedia.cadmium.core.git.GitService;

public class SwitchBranchTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private DelayedGitServiceInitializer service;
  private String branch;
  private Future<Boolean> previousTask;
  
  public SwitchBranchTask(DelayedGitServiceInitializer service, String branchName, Future<Boolean> previousTask) {
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
    GitService service = this.service.getGitService();
    try {
      service.fetchRemotes();
      if(service.isBranch(branch) || service.isTag(branch)) {
        log.info("Switching branch to {}",branch);
        service.switchBranch(branch);
      } else {
        throw new Exception("The branch ["+branch+"] does not exist");
      }
    } finally {
      this.service.releaseGitService();
    }
    return true;
  }

}
