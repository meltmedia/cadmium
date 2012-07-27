package com.meltmedia.cadmium.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigManager {

  private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

  
  public static Properties getPropertiesByFile(File configFile) {

    Properties properties = new Properties();    
    Reader reader = null;
    try{

      log.info("configFile path: {}", configFile.getPath());
      reader = new FileReader(configFile);
      properties.load(reader);
      
      for(Object key : properties.keySet()) {
        System.out.println("Fetched properties by file: " + properties.getProperty(key.toString()));
      }
    }
    catch(Exception e) {

      log.warn("Failed to load "+configFile.getAbsolutePath());
    }
    finally {

      IOUtils.closeQuietly(reader);
    }

    return properties;

  }

  public static Properties loadProperties(Properties properties, File configFile) {

    if( !configFile.exists() || !configFile.canRead()) return properties;
    
    Reader reader = null;
    try{
      
      log.info("configFile path: {}", configFile.getPath());

      reader = new FileReader(configFile);
      properties.load(reader);
      
      for(Object key : properties.keySet()) {
        System.out.println("Loaded properties: " + properties.getProperty(key.toString()));
      }
    }
    catch(Exception e) {

      log.warn("Failed to load "+configFile.getAbsolutePath());
    }
    finally {

      IOUtils.closeQuietly(reader);
    }
    
    return properties;
  }

  public static Properties getSystemProperties() {

    Properties properties = new Properties();     
    properties.putAll(System.getenv());
    properties.putAll(System.getProperties());
            
    return properties;

  }

  public static Properties getPropertiesByContext(ServletContext context, String path) {

    Properties properties = new Properties();
    Reader reader = null;
    try{

      reader = new InputStreamReader(context.getResourceAsStream(path), "UTF-8");
      properties.load(reader);
      
      for(Object key : properties.keySet()) {
        System.out.println("Fetched properties by Context: " + properties.getProperty(key.toString()));
      }

    } 
    catch(Exception e) {

      log.warn("Failed to load "+path);
    }
    finally {

      IOUtils.closeQuietly(reader);
    }

    return properties;
  }
  
  public static Properties getPropertiesByFileName(String fileName) {
    
    Properties properties = new Properties();
    if(new File(fileName).canRead()) {
      
      FileInputStream in = null;
      try {
        
        in = new FileInputStream(fileName);
        properties.load(in);
      }
      catch(Exception e){
        
        log.warn("Failed to read in vault properties file.", e);
      } 
      finally {
        
        if(in != null) {
          
          try{
            
            in.close();
          } 
          catch(Exception e){}
        }
      }
    }
    
    return properties;
  }

  public static void persistProperties(Properties properties, String fileName) {

    File propsFile = new File(fileName);
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
            catch(Exception e){
              
              log.warn("Failed to close output stream for writing properties.", e);
            }
          }
        }
      }
    }
  }

  private static void ensureDirExists(String dir) {
    File file = new File(dir);
    if(!file.exists()) {
      file.mkdirs();
    }
  }


}
