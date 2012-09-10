package com.meltmedia.cadmium.core.config;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
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
public class ConfigManager implements Closeable {

  private final Logger log = LoggerFactory.getLogger(ConfigManager.class);

  private Properties defaultProperties;    
  private PropertiesReader reader = new PropertiesReaderImpl();
  private PropertiesWriter writer = new PropertiesWriterImpl();
  private ConfigurationParser stagedConfigParser;
  private ConfigurationParser liveConfigParser;
  private CountDownLatch latch;

  @Inject
  protected ConfigurationParserFactory configParserFactory;

  public ConfigManager() {

    latch = new CountDownLatch(1);
  }

  public Properties getProperties(File configFile) {   

    return reader.getProperties(configFile, log);
  }

  /**
   * Adds properties from file to the default properties if the file exists.
   * 
   * @param configFile
   * @return default properties instance
   */
  public Properties appendToDefaultProperties(File configFile) {

    if(defaultProperties != null && configFile.canRead()) {

      defaultProperties = appendProperties(defaultProperties, configFile);      
    }

    return defaultProperties;
  }

  /**
   * Add new properties to an existing Properties object 
   * 
   */
  public Properties appendProperties(Properties properties, File configFile) {    

    if(!configFile.exists()) {

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
   * Read in properties based on an InputStream
   * 
   */
  public Properties getPropertiesByInputStream(InputStream stream) {   

    return reader.getProperties(stream, log);
  }   


  /**
   * Persist properties that are not system env or other system properties, in a thread synchronized manner
   * 
   */
  public synchronized void persistProperties(Properties properties, File propsFile, String message) {
    Properties toWrite = new Properties();
    for(String key : properties.stringPropertyNames()) {

      if(System.getProperties().containsKey(key) && !properties.getProperty(key).equals(System.getProperty(key))) {

        toWrite.setProperty(key, properties.getProperty(key));
      } 
      else if(System.getenv().containsKey(key) && !properties.getProperty(key).equals(System.getenv(key))) {

        toWrite.setProperty(key, properties.getProperty(key));
      } 
      else if(!System.getProperties().containsKey(key) && !System.getenv().containsKey(key)){

        toWrite.setProperty(key, properties.getProperty(key));
      }
    }
    writer.persistProperties(toWrite, propsFile, message, log);

  }

  public Properties getDefaultProperties() {
    return defaultProperties;
  }

  public void setDefaultProperties(Properties defaultProperties) {
    this.defaultProperties = defaultProperties;
  } 

  public void parseConfigurationDirectory(File directory) {

    ConfigurationParser parser = configParserFactory.newInstance();
    try {

      parser.parseDirectory(directory);
      stagedConfigParser = parser;
    } 
    catch (Exception e) {

      log.error("Problem gettign config data from directory: {}", directory);
    }
  }

  public void makeConfigParserLive() {

    if(stagedConfigParser != null) {

      liveConfigParser = stagedConfigParser;
      latch.countDown();
    }
  }

  public <T> T getConfiguration(String key, Class<T> type) throws ConfigurationNotFoundException {    

    try {

      latch.await();
    }
    catch (InterruptedException e) {

      log.error("Latch interrupted, Not able to get configuration for key: {}, and class: {}", key, type);
      return null;
    }

    return liveConfigParser.getConfiguration(key, type); 
  }

  @Override
  public void close() throws IOException {

    latch.countDown();    
  }  

}
