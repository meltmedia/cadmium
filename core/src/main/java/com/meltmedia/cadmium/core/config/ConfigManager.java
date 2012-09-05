package com.meltmedia.cadmium.core.config;

import java.io.File;
import java.util.Properties;

import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.config.impl.PropertiesReaderImpl;
import com.meltmedia.cadmium.core.config.impl.PropertiesWriterImpl;

/**
 * This centralizes and manages how other classes read and write to properties files. 
 * 
 * @author Brian Barr
 */

@Singleton
public class ConfigManager {

  private final Logger log = LoggerFactory.getLogger(ConfigManager.class);
  
  private Properties defaultProperties;    
  private PropertiesReader reader = new PropertiesReaderImpl();
  private PropertiesWriter writer = new PropertiesWriterImpl();

  public Properties getProperties(File configFile) {   
    
    return reader.getProperties(configFile, log);
  }
  
  /**
   * Add new properties to an existing Properties object 
   * 
   */
  public Properties appendProperties(Properties properties, File configFile) {    
    
    if( !configFile.exists() ) {
      
      return properties;
    }
    
    return reader.appendProperties(properties, configFile, log);
  }

  /**
   * Read in The system properties
   * 
   */
  public Properties getSystemProperties() {

    Properties properties = new Properties();     
    properties.putAll(System.getenv());
    properties.putAll(System.getProperties());
            
    return properties;
  }

  /**
   * Read in properties based on a ServletContext and a path to a config file
   * 
   */
  public Properties getPropertiesByContext(ServletContext context, String path) {   

    return reader.getProperties(context, path, log);
  }   
  

  /**
   * Read in properties based on a ServletContext and a path to a config file
   * 
   */
  public void persistProperties(Properties properties, File propsFile, String message) {
    
    writer.persistProperties(properties, propsFile, message, log);
    
  }
  
  public void logProperties( Logger log, Properties properties, String name ) {
    if( log.isDebugEnabled() ) {
      StringBuilder sb = new StringBuilder().append(name).append(" properties:\n");
      for(Object key : properties.keySet()) {
        sb.append("  ").append(key.toString()).append(properties.getProperty(key.toString())).append("\n");
      }
      log.debug(sb.toString());
    }
  }  

  public Properties getDefaultProperties() {
    return defaultProperties;
  }

  public void setDefaultProperties(Properties defaultProperties) {
    this.defaultProperties = defaultProperties;
  }
 
}
