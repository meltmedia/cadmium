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
package com.meltmedia.cadmium.core.meta;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SiteConfigProcessor {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private Set<ConfigProcessor> processors;
  
  @Inject
  public SiteConfigProcessor(Set<ConfigProcessor> processors, @Named("contentDir") String contentDir) throws Exception {
    this.processors = processors;
  }
  
  public void processDir(String contentDirectory) throws Exception {
    log.info("Checking for a META-INF directory in {}", contentDirectory);
    String metaDir = new File(contentDirectory, "META-INF").getAbsoluteFile().getAbsolutePath();
    if(metaDir != null) {
      if(processors != null) {
        boolean failed = false;
        log.info("Running {} processor[s] for {} directory", processors.size(), metaDir);
        for(ConfigProcessor processor : processors) {
          try {
            log.info("Running {}", processor.getClass().getName());
            processor.processFromDirectory(metaDir);
          } catch(IOException e) {
            throw e;
          } catch(Exception e){
            log.error("Failed to process config", e);
            failed = true;
          }
        }
        if(failed) {
          throw new Exception("One or more configs failed! See log for details.");
        }
      } else {
        log.warn("No config processors exist in this context");
      }
    }
  }  
  
  public void makeLive() {
    if(processors != null) {
      for(ConfigProcessor proc : processors) {
        proc.makeLive();
      }
    }
  }
  
}
