package com.meltmedia.cadmium.core.meta;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.meltmedia.cadmium.core.FileSystemManager;

@Singleton
public class SiteConfigProcessor {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private List<ConfigProcessor> processors;
  
  @Inject
  public SiteConfigProcessor(List<ConfigProcessor> processors, @Named("contentDir") String contentDir) throws Exception {
    this.processors = processors;
    if(contentDir != null) {
      this.processDir(contentDir);
      this.makeLive();
    }
  }
  
  public void processDir(String contentDirectory) throws Exception {
    log.info("Checking for a META-INF directory in {}", contentDirectory);
    String metaDir = FileSystemManager.getChildDirectoryIfExists(contentDirectory, "META-INF");
    if(metaDir != null) {
      log.info("meta directory exists {}", metaDir);
      if(processors != null) {
        boolean failed = false;
        log.info("Running {} processor[s] for {} directory", processors.size(), metaDir);
        for(ConfigProcessor processor : processors) {
          try {
            log.info("Running {}", processor.getClass().getName());
            processor.processFromDirectory(metaDir);
          } catch(Exception e){
            log.error("Failed to processes config", e);
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
