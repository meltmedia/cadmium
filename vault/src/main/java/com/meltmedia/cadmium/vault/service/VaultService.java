package com.meltmedia.cadmium.vault.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.vault.SafetyMissingException;
import com.meltmedia.cadmium.vault.VaultListener;

@Singleton
public class VaultService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private VaultListener listener;
  
  protected ResourceManager resourceManager;
  private ResourceFetcher fetcher;
  
  private Timer resourceManagerTimer;
  
  protected Map<String, String> guidCache = new HashMap<String, String>();
 
  private Integer intervalSeconds;
  
  VaultService(){}
  
  @Inject
  public VaultService( 
      @Named(VaultConstants.CACHE_DIRECTORY) String cacheDir, 
      @Named(VaultConstants.PROPERTIES_FILE_NAME) String propertiesFile,
      @Named(VaultConstants.WATCH_INTERVAL_SECONDS) Integer intervalSeconds,
      ResourceFetcher fetcher) {
    
    this.intervalSeconds = intervalSeconds;
    
    File cachePropsFile = new File(propertiesFile);
    if(!cachePropsFile.isAbsolute()) {
      cachePropsFile = new File(cacheDir, propertiesFile);
    }
    
    propertiesFile = cachePropsFile.getAbsoluteFile().getAbsolutePath();
    
    this.fetcher = fetcher;
    
    this.resourceManager = new ResourceManager(this, propertiesFile, cacheDir);
    
    startTimers();
  }
  
  private void startTimers() {
    resourceManagerTimer = new Timer();
    resourceManagerTimer.schedule(this.resourceManager, new Date(), intervalSeconds * 1000l);
  }
  
  public String getSafety(String guid) throws SafetyMissingException, IOException {
    synchronized(guidCache) {
      String resource = guidCache.get(guid);
      
      if(resource == null) {
        resource = resourceManager.getSafety(guid);
        if(resource != null) {
          guidCache.put(guid, resource);
        }
      } else {
        synchronized(resourceManager.vaultProperties) {
          resourceManager.updateAccessTime(guid);
        }
      }
      
      return resource;
    }
  }
  
  @Inject
  public void setVaultListener(VaultListener listener) {
    this.listener = listener;
  }
  
  VaultListener getVaultListener() {
    return listener;
  }
  
  void notifyListener(String guids[]) {
    if(listener != null) {
      log.debug("Notifying listener of safety update!");
      synchronized(guidCache) {
        guidCache.clear();
      }
      listener.safetyUpdated(guids);
    }
  }
  
  ResourceFetcher getFetcher() {
    return this.fetcher;
  }
  
  @Override
  public void finalize() {
    log.info("Shutting down!");
    if(resourceManagerTimer != null) {
      resourceManagerTimer.cancel();
    }
    resourceManagerTimer = null;
  }
 }
