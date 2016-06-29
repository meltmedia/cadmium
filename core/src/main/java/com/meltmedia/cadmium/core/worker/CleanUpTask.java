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
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.meltmedia.cadmium.core.commands.UpdateFailedCommandAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.commands.ContentUpdateRequest;

public class CleanUpTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private Properties configProperties;
  private Future<Boolean> previousTask;
  private CoordinatedWorkerListener listener;
  private String configKey;
  private ContentUpdateRequest contentUpdateBody;
  
  public CleanUpTask(String configKey, CoordinatedWorkerListener listener, Properties configProperties, ContentUpdateRequest contentUpdateBody, Future<Boolean> previousTask) {
    this.configProperties = configProperties;
    this.previousTask = previousTask;
    this.contentUpdateBody = contentUpdateBody;
    this.listener = listener;
    this.configKey = configKey;
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
        Throwable throwable = ExceptionUtils.getRootCause(e);
        log.error("Work failed!", throwable);

        String failureReason = StringUtils.isEmpty(throwable.getMessage()) ? UpdateFailedCommandAction.FAILED_LOG_MESSAGE : throwable.getMessage();
        contentUpdateBody.setFailureReason(throwable.getClass().getSimpleName()+ ": " + failureReason);
        listener.workFailed(contentUpdateBody);
        return false;
      }
    }
    
    log.info("Cleaning up directories");
    
    FileSystemManager.cleanUpOld(configProperties.getProperty(configKey), 1);
    
    return true;
  }

}
