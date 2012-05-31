package com.meltmedia.cadmium.core.worker;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.core.history.HistoryManager;

public class UpdateConfigTask implements Callable<Boolean> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private GitService service;
  private Map<String, String> properties;
  private Properties configProperties;
  private HistoryManager manager;
  private Future<Boolean> previousTask;
  
  public UpdateConfigTask(GitService service, Map<String, String> properties, Properties configProperties, HistoryManager manager, Future<Boolean> previousTask) {
    this.service = service;
    this.properties = properties;
    this.configProperties = configProperties;
    this.manager = manager;
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
    log.info("Updating config.properties file");
    String lastUpdatedDir = properties.get("nextDirectory");
    
    String baseDirectory = service.getBaseDirectory();
    
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
    updatedProperties.setProperty("git.ref.sha", service.getCurrentRevison());
    configProperties.setProperty("git.ref.sha", service.getCurrentRevison());
    
    if(manager != null) {
      manager.logEvent(service.getBranchName(), service.getCurrentRevison(), "SYNC".equals(properties.get("comment")) ? "AUTO" : "", lastUpdatedDir, properties.get("comment"), true);
    }
    
    try{
      updatedProperties.store(new FileWriter(new File(baseDirectory, "config.properties")), null);
    } catch(Exception e) {
      log.warn("Failed to write out config file", e);
    }
    
    return true;
  }

}
