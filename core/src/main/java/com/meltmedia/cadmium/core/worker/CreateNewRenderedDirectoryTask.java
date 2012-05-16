package com.meltmedia.cadmium.core.worker;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.git.GitService;

public class CreateNewRenderedDirectoryTask implements Callable<Boolean> {
  
  private GitService service;
  private String lastDirectory;
  private Map<String, String> properties;
  private Future<Boolean> previousTask;
  
  public CreateNewRenderedDirectoryTask(GitService service, String lastDirectory, Map<String, String> properties, Future<Boolean> previousTask) {
    this.service = service;
    this.lastDirectory = lastDirectory;
    this.properties = properties;
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
    String nextDirectory = FileSystemManager.getNextDirInSequence(lastDirectory);
    if(nextDirectory != null) {
      GitService git = GitService.cloneRepo(service.getRepositoryDirectory(), nextDirectory);
      if(git != null) {
        git.close();
        FileSystemManager.deleteDeep(FileSystemManager.getChildDirectoryIfExists(nextDirectory, ".git"));
        properties.put("nextDirectory", nextDirectory);
      } else {
        return false;
      }
    } else {
      return false;
    }
    return true;
  }

}
