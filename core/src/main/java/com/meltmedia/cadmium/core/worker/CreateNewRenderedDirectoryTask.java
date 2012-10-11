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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.commands.ContentUpdateRequest;
import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
import com.meltmedia.cadmium.core.git.GitService;

public abstract class CreateNewRenderedDirectoryTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private DelayedGitServiceInitializer service;
  private String lastDirectory;
  private Future<Boolean> previousTask;

  private ContentUpdateRequest body;
  
  public CreateNewRenderedDirectoryTask(DelayedGitServiceInitializer service, String lastDirectory, ContentUpdateRequest body, Future<Boolean> previousTask) {
    this.service = service;
    this.lastDirectory = lastDirectory;
    this.body = body;
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
      log.info("Creating a new directory for the git service snapshot.");
      try{
        String nextDirectory = FileSystemManager.getNextDirInSequence(lastDirectory);
        if(nextDirectory != null) {
          GitService git = GitService.cloneRepo(service.getRepositoryDirectory(), nextDirectory);
          if(git != null) {
            git.close();
            FileSystemManager.deleteDeep(FileSystemManager.getChildDirectoryIfExists(nextDirectory, ".git"));
            setNextDirectory(nextDirectory);
          } else {
            log.warn("Failed to clone repo to "+nextDirectory);
            return false;
          }
        } else {
          log.warn("Failed to get next directory");
          return false;
        }
      } catch(Exception e) {
        log.error("Failed to create new rendered directory", e);
        return false;
      }
    } finally {
      this.service.releaseGitService();
    }
    return true;
  }
  
  public abstract void setNextDirectory( String nextDirectory );

}
