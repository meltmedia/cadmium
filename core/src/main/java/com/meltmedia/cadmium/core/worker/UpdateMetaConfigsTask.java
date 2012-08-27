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

import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;

public class UpdateMetaConfigsTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private SiteConfigProcessor processor;
  private Future<Boolean> previousTask;
  private Map<String, String> properties;
  
  public UpdateMetaConfigsTask(SiteConfigProcessor processor, Map<String, String> properties, Future<Boolean> previousTask) {
    this.processor = processor;
    this.previousTask = previousTask;
    this.properties = properties;
  }

  @Override
  public Boolean call() throws Exception {
    if(previousTask != null) {
      Boolean lastResponse = previousTask.get();
      if(lastResponse != null && !lastResponse.booleanValue() ) {
        throw new Exception("Previous task failed");
      }
    }
    String nextDirectory = properties.get("nextDirectory");
    log.info("Processing META-INF directory [{}] for configs if exists", nextDirectory);
    if(nextDirectory != null) {
      if(processor != null) {
        log.info("Processing!!!");
        processor.processDir(nextDirectory);
      } else {
        log.warn("The SiteConfigProcessor is not set in this context!");
      }
    } else {
      return false;
    }
    return true;
  }

}
