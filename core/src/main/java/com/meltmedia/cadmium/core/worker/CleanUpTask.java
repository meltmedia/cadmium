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

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.FileSystemManager;

public class CleanUpTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private Properties configProperties;
  private Future<Boolean> previousTask;
  private CoordinatedWorkerListener listener;
  private Map<String, String> properties;
  
  public CleanUpTask(CoordinatedWorkerListener listener, Properties configProperties, Map<String, String> properties, Future<Boolean> previousTask) {
    this.configProperties = configProperties;
    this.previousTask = previousTask;
    this.properties = properties;
    this.listener = listener;
  }

  @Override
  public Boolean call() throws Exception {
    if(previousTask != null) {
      try{
        Boolean lastResponse = previousTask.get();
        if(lastResponse != null && !lastResponse.booleanValue() ) {
          throw new Exception("Previous task failed");
        }
      } catch(Exception e) {
        log.warn("Work failed!", e);
        listener.workFailed(properties.get("repo"), properties.get("branch"), properties.get("sha"), properties.get("openId"), properties.get("uuid"));
        return false;
      }
    }
    
    log.info("Cleaning up content directories");
    
    FileSystemManager.cleanUpOld(configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"), 1);
    
    return true;
  }

}
