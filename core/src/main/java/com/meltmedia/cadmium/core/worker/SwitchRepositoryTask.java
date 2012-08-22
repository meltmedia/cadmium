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
