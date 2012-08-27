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

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
import com.meltmedia.cadmium.core.git.GitService;

public class PullUpdateTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private DelayedGitServiceInitializer service;
  private Future<Boolean> previousTask;
  private Properties configProperties;
  
  public PullUpdateTask(DelayedGitServiceInitializer service, Properties configProperties, Future<Boolean> previousTask) {
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
    GitService service = this.service.getGitService();
    try {
      if(!service.isTag(service.getBranchName())) {
        log.info("Pulling latest Github updates.");
        boolean retVal = service.pull();
        configProperties.setProperty("updating.to.sha", service.getCurrentRevision());
        configProperties.setProperty("updating.to.branch", service.getBranchName());
        return retVal;
      } else {
        //configProperties.setProperty("updating.to.sha", service.getCurrentRevision());
        configProperties.setProperty("updating.to.branch", service.getBranchName());
        log.info("Skipping pull since tags cannot update.");
        return true;
      }
    } finally {
      this.service.releaseGitService();
    }
  }

}
