/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.worker;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.git.GitService;

public class UpdateConfigTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private GitService service;
  private Map<String, String> properties;
  private Properties configProperties;
  private Future<Boolean> previousTask;
  
  public UpdateConfigTask(GitService service, Map<String, String> properties, Properties configProperties, Future<Boolean> previousTask) {
    this.service = service;
    this.properties = properties;
    this.configProperties = configProperties;
    this.previousTask = previousTask;
  }

  @Override
  public Boolean call() throws Exception {
    final String branch = configProperties.getProperty("branch");
    final String revision = service.isTag(branch) ? null : configProperties.getProperty("git.ref.sha");
    try {
      if(previousTask != null) {
        Boolean lastResponse = previousTask.get();
        if(lastResponse != null && !lastResponse.booleanValue() ) {
          throw new ExecutionException("Previous task failed", new Exception());
        }
      }
      log.info("Updating config.properties file");
      String lastUpdatedDir = properties.get("nextDirectory");
      
      String baseDirectory = FileSystemManager.getParent(service.getBaseDirectory());
      
      Properties updatedProperties = new Properties();
      
      if(configProperties.containsKey("com.meltmedia.cadmium.lastUpdated")) {
        updatedProperties.setProperty("com.meltmedia.cadmium.previous", configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"));
      }
      updatedProperties.setProperty("com.meltmedia.cadmium.lastUpdated", lastUpdatedDir);
      configProperties.setProperty("com.meltmedia.cadmium.lastUpdated", lastUpdatedDir);
      if(configProperties.containsKey("branch")) {
        updatedProperties.setProperty("branch.last", configProperties.getProperty("branch"));
      }
      updatedProperties.setProperty("branch", service.getBranchName());
      configProperties.setProperty("branch", service.getBranchName());
      if(configProperties.containsKey("git.ref.sha")) {
        updatedProperties.setProperty("git.ref.sha.last", configProperties.getProperty("git.ref.sha"));
      }
      updatedProperties.setProperty("git.ref.sha", service.getCurrentRevision());
      configProperties.setProperty("git.ref.sha", service.getCurrentRevision());
      
      properties.put("BranchName", service.getBranchName());
      properties.put("CurrentRevision", service.getCurrentRevision());
      
      if(configProperties.containsKey("updating.to.sha")) {
        configProperties.remove("updating.to.sha");
      }
      if(configProperties.containsKey("updating.to.branch")) {
        configProperties.remove("updating.to.branch");
      }
      
      try{
        updatedProperties.store(new FileWriter(new File(baseDirectory, "config.properties")), null);
      } catch(Exception e) {
        log.warn("Failed to write out config file", e);
      }
      
      return true;
    } catch(ExecutionException e) {
      new Timer().schedule(new TimerTask() {
        public void run() {
          try {
            log.info("Reverting to last branch["+branch+"] and revision ["+revision+"]!");
            if(!service.getBranchName().equals(branch)) {
              service.switchBranch(branch);
            }
            if(revision != null && !service.getCurrentRevision().equals(revision)) {
              service.resetToRev(revision);
            }
          } catch(Exception e1) {
            log.error("Failed to revert", e1);
          }
        }
      }, 250l);
      throw e;
    }
  }

}
