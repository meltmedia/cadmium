package com.meltmedia.cadmium.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigManager {

  private final Logger log = LoggerFactory.getLogger(getClass());
        
  @Inject
  @Named("contentDir")
  protected String initialContentDir;
  
 
  
  public Properties getProperties(File configFile) {
    
    Properties properties = new Properties();    
    Reader reader = null;
    try{
     
      reader = new FileReader(configFile);
      properties.load(reader);
    }
    catch(Exception e) {
      
      log.warn("Failed to load "+configFile.getAbsolutePath());
    }
    finally {
     
      IOUtils.closeQuietly(reader);
    }
    
    return properties;
   
  }
  
  public Properties getSystemProperties() {
    
    Properties properties = new Properties();     
    properties.putAll(System.getenv());
    properties.putAll(System.getProperties());
    
    return properties;
    
  }
  
  public Properties getProperties(ServletContext context, String path) {
   
    Properties properties = new Properties();
    Reader reader = null;
    try{
      
      reader = new InputStreamReader(context.getResourceAsStream(path), "UTF-8");
      properties.load(reader);
      
    } 
    catch(Exception e) {
      
      log.warn("Failed to load "+path);
    }
    finally {
      
      IOUtils.closeQuietly(reader);
    }
    
    return properties;
  }
  
  public void persistProperties(Properties properties, File propsFile) {
    
    if(propsFile.canWrite() || !propsFile.exists()) {
      
      if(!properties.isEmpty()) {
       
        ensureDirExists(propsFile.getParent());
        FileOutputStream out = null;
        try {
          
          out = new FileOutputStream(propsFile.getAbsolutePath());
          properties.store(out, null);
        }
        catch(Exception e) {
          
          log.warn("Failed to persist vault properties file.", e);
        }
        finally {
          
          if(out != null) {
            
            try {
              
              out.close();
            } 
            catch(Exception e){}
          }
        }
      }
    }
  }
  
  // update a property by key with passed value
  public void updateProperty(Properties properties, String key, String value) {
    
    properties.setProperty(key, value);
  }
  
  // remove a property by key
  public void removeProperty(Properties properties, String key) {
    
    properties.remove(key);
  }
  
  private void ensureDirExists(String dir) {
    File file = new File(dir);
    if(!file.exists()) {
      file.mkdirs();
    }
  }
    
  
}
