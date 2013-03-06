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

import com.meltmedia.cadmium.core.ConfigurationGitService;
import com.meltmedia.cadmium.core.CurrentWarName;
import com.meltmedia.cadmium.core.SharedContentRoot;
import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
import com.meltmedia.cadmium.core.git.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class CheckConfigInitializedTask implements Callable<Boolean> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private Callable<GitService> taskToCheck = null;
  private ExecutorService pool = null;
  private Future<GitService> futureTask = null;
  private DelayedGitServiceInitializer gitInit = null;
  private String sharedContentRoot = null;
  private String warName = null;
  
  @Inject
  public CheckConfigInitializedTask(ConfigInitializeTask taskToCheck, @ConfigurationGitService DelayedGitServiceInitializer gitInit, @SharedContentRoot String contentRoot, @CurrentWarName String warName) {
    this.taskToCheck = taskToCheck;
    this.gitInit = gitInit;
    this.sharedContentRoot = contentRoot;
    this.warName = warName;
  }
  
  public CheckConfigInitializedTask setFuture(Future<GitService> futureTask) {
    this.futureTask = futureTask;
    return this;
  }
  
  public CheckConfigInitializedTask setExecutor(ExecutorService pool) {
    this.pool = pool;
    return this;
  }

  @Override
  public Boolean call() throws Exception {
    GitService git = null;
    try {
      if((git = futureTask.get()) == null) {
        File gitCheckout = new File(new File(sharedContentRoot, warName), "git-checkout");
        if(gitCheckout.exists() && gitCheckout.isDirectory()) {
          try {
            git = GitService.createGitService(gitCheckout.getAbsolutePath());
            gitInit.setGitService(git);
          } catch(Exception e){
            logger.warn("Config Git Service did not initialize.", e);
          }
        }
        return true;
      }
    } catch(Exception e){
      try {
        logger.info("Config initialize task `" + taskToCheck + "` failed.  Retrying in 30 seconds.", e);
        Thread.sleep(30000l);
      } catch(Exception e1){}
      try {
        futureTask = pool.submit(taskToCheck);
        pool.submit(this);
      } catch(RejectedExecutionException e1){
        logger.info("ExecutorService has been shutdown.", e1);
      }
      return true;
    }
    gitInit.setGitService(git);
    return true;
  }

}
