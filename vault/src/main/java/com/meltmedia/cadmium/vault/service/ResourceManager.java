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
package com.meltmedia.cadmium.vault.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.vault.SafetyMissingException;

public class ResourceManager extends TimerTask {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final SimpleDateFormat propertiesFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  
  private VaultService service;
  private ResourceFetcher fetcher;
  private String propertiesFileName;
  Properties vaultProperties = new Properties();
  private String cacheDir;
  
  ResourceManager() {}
  
  public ResourceManager(VaultService service, String fileName, String cacheDir) {
    this.service = service;
    if(service != null) {
      this.fetcher = service.getFetcher();
    }
    this.propertiesFileName = fileName;
    this.cacheDir = cacheDir;
       
    vaultProperties = ConfigManager.getPropertiesByFileName(propertiesFileName);
    //readInPropertiesFile();
    
  }
  
  /*private void readInPropertiesFile() {    
    if(new File(propertiesFileName).canRead()) {
      FileInputStream in = null;
      try {
        in = new FileInputStream(propertiesFileName);
        vaultProperties.load(in);
      } catch(Exception e){
        log.warn("Failed to read in vault properties file.", e);
      } finally {
        if(in != null) {
          try{
            in.close();
          } catch(Exception e){}
        }
      }
    }
  }*/
  
  public String getSafety(String guid) throws SafetyMissingException, IOException {
    synchronized(vaultProperties) {
      String safety = null;
      if(safetyExistsOnDisk(guid)) {
        updateAccessTime(guid);
        safety = getSafetyFromDisk(guid);
      } else {
        safety = fetcher.fetch(guid, null);
        if(safety != null) {
          updateAccessTime(guid);
          cacheToDisk(guid, safety);
        }
      }
      return safety;
    }
  }
  
  private File getSafetyFile(String guid) {
    File resourceFile = new File(cacheDir, guid + ".res");
    return resourceFile.getAbsoluteFile();
  }
  
  private boolean safetyExistsOnDisk(String guid) {
    File safetyFile = getSafetyFile(guid);
    if(safetyFile.exists() && safetyFile.canRead()) {
      return true;
    }
    return false;
  } 
  
  private String getSafetyFromDisk(String guid) throws IOException {
    FileInputStream in = null;
    String content = null;
    try {
      in = new FileInputStream(getSafetyFile(guid));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      streamCopy(in, out);
      
      content = out.toString();
      
    } finally {
      if(in != null) {
        try {
          in.close();
        } catch(Exception e){}
      }
    }
    return content;
  }
  
  private void cacheToDisk(String guid, String content) throws IOException {
    ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
    FileOutputStream out = null;
    
    try{
      ensureCacheDirExists(cacheDir);
      File safetyFile = getSafetyFile(guid);
      out = new FileOutputStream(safetyFile);
      
      streamCopy(in, out);
      
    } finally {
      if(out != null) {
        try {
          out.close();
        } catch(Exception e){}
      }
    }
  }

  private void ensureCacheDirExists(String cacheDir) {
    File cacheFile = new File(cacheDir);
    if(!cacheFile.exists()) {
      cacheFile.mkdirs();
    }
  }
  
  public void updateAccessTime(String guid) {
    vaultProperties.setProperty(guid, propertiesFormat.format(new Date()));
  }
  

  @Override
  public void run() {
    try {
      log.debug("Running check to see if the safety has updated.");
      String updated[] = updateSafety();
      if(updated != null && updated.length > 0) {
        service.notifyListener(updated);
      }
    } catch(SafetyMissingException e) {
      if(service.getVaultListener() != null) {
        service.getVaultListener().safetyMissing();
      }
      log.warn("Safety is missing.", e);
    } catch(Throwable e) {
      log.error("Safety update failed trying again later.", e);
      if(service.getVaultListener() != null) {
        service.getVaultListener().safetyUpdateFailed();
      }
    }
    
    purgeUnusedSafety();
    ConfigManager.persistProperties(vaultProperties, propertiesFileName);
    //persistVaultProperties();
  }
  
  private void purgeUnusedSafety() {
    synchronized(vaultProperties) {
      List<String> vaultKeys = new ArrayList<String>(vaultProperties.stringPropertyNames());
      for(String vaultKey: vaultKeys) {
        Date lastAccessed = getLastAccessTime(vaultKey);
        if(lastAccessed == null || lastAccessed.before(new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000l)))) {
          File safetyFile = getSafetyFile(vaultKey);
          if(safetyFile.exists() ){
            if(safetyFile.delete()) {
              vaultProperties.remove(vaultKey);
            }
          }
        }
      }
    }
  }

  public String[] updateSafety() throws SafetyMissingException, IOException {
    synchronized(vaultProperties) {
      List<String> updated = new ArrayList<String>();
      log.debug("Updating safety!");
      for(Object vaultKey : vaultProperties.keySet()) {
        log.debug("Updating {}", vaultKey);
        String oldResource = getSafetyFromDisk(vaultKey.toString());
        Date lastModified = getLastModifiedTime(vaultKey.toString());
        updateAccessTime(vaultKey.toString());
        String resource = null;
        log.debug("Old resource [{}]", oldResource);
        resource = fetcher.fetch(vaultKey.toString(), lastModified);
        log.debug("New resource [{}]", resource);
        if(resource != null && resource.trim().length() > 0) {
          log.debug("Resource was updated!");
          if(oldResource == null || !oldResource.equals(resource)) {
            updated.add(vaultKey.toString());
          }
          cacheToDisk(vaultKey.toString(), resource);
        }
      }
      return updated.toArray(new String[] {});
    }
  }
  
  private Date getLastAccessTime(String guid) {
    if(vaultProperties.containsKey(guid)) {
      try {
        return propertiesFormat.parse(vaultProperties.getProperty(guid));
      } catch(Exception e) {
        log.error("This should not have happened!", e);
      }
    }
    return null;
  }
  
  private Date getLastModifiedTime(String guid) {
    File safetyFile = getSafetyFile(guid);
    if(safetyFile.exists()) {
      return new Date(safetyFile.lastModified());
    }
    return null;
  }
  
  /*public void persistVaultProperties() {
    File propsFile = new File(propertiesFileName);
    if(propsFile.canWrite() || !propsFile.exists()) {
      if(!vaultProperties.isEmpty()) {
        ensureCacheDirExists(propsFile.getParent());
        FileOutputStream out = null;
        try {
          out = new FileOutputStream(propsFile.getAbsolutePath());
          vaultProperties.store(out, null);
        } catch(Exception e) {
          log.warn("Failed to persist vault properties file.", e);
        } finally {
          if(out != null) {
            try {
              out.close();
            } catch(Exception e){}
          }
        }
      }
    }
  }*/
  
  public static void streamCopy(InputStream streamIn, OutputStream streamOut) throws IOException {
    ReadableByteChannel input = Channels.newChannel(streamIn);
    WritableByteChannel output = Channels.newChannel(streamOut);
    
    ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    
    while (input.read(buffer) != -1) {
      buffer.flip();
  
      output.write(buffer);
  
      buffer.compact();
    }   
    
    buffer.flip();
  
    // Make sure the buffer is empty
    while (buffer.hasRemaining()) {
      output.write(buffer);
    } 
  }
  
}
