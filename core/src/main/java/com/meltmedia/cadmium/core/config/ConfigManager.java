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
package com.meltmedia.cadmium.core.config;

import com.google.inject.Inject;
import com.meltmedia.cadmium.core.config.impl.PropertiesReaderImpl;
import com.meltmedia.cadmium.core.config.impl.PropertiesWriterImpl;
import com.meltmedia.cadmium.core.util.WarUtils;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * This centralizes and manages how other classes read and write to properties files. 
 * 
 * @author Brian Barr
 * @author John McEntire
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
  private Set<ConfigurationListener<?>> listeners = Collections.synchronizedSet(new HashSet<ConfigurationListener<?>>());
  private File defaultPropertiesLocation;
  private String warFileName;

  @Inject
  protected ConfigurationParserFactory configParserFactory;

  public ConfigManager() {

    latch = new CountDownLatch(1);
  }

  public ConfigManager(ServletContext ctx) {
    warFileName = WarUtils.getWarName(ctx);
    latch = new CountDownLatch(1);
  }
  
  @SuppressWarnings("rawtypes")
  @Inject(optional=true)
  protected void getListenersFromGuice(Set<ConfigurationListener> listeners) {
    if(listeners != null) {
      log.debug("Installing " + listeners.size() + " configuration listeners from guice.");
      for(ConfigurationListener listener : listeners) {
        log.debug("Registering configuration listener: "+listener);
        this.listeners.add(listener);
      }
    }
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
   * Writes out default properties file to the preset location set from {@link ConfigManager#setDefaultPropertiesFile(File)}.
   */
  public synchronized void persistDefaultProperties() {
    if(defaultProperties != null && defaultPropertiesLocation != null) {
      persistProperties(defaultProperties, defaultPropertiesLocation, null);
    }
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
  
  public void setDefaultPropertiesFile(File defaultPropertiesLocation) {
    this.defaultPropertiesLocation = defaultPropertiesLocation;
  }

  public void parseConfigurationDirectory(File directory) {

    ConfigurationParser parser = configParserFactory.newInstance();
    try {

      parser.parseDirectory(directory);
      stagedConfigParser = parser;
    } 
    catch (Exception e) {

      log.error("Problem getting config data from directory: {}", directory);
      log.error("And the Exception is: ", e);
    }
  }

  /**
   * This notifies all registered listeners then make a staged configuration live.
   */
  public void makeConfigParserLive() {

    if(stagedConfigParser != null) {
      notifyListeners(listeners, stagedConfigParser, log);
      
      liveConfigParser = stagedConfigParser;
      latch.countDown();
    }
  }
  
  /**
   * This notifies listeners from a given set of the state of the configurations from a {@link ConfigurationParser}.
   * @param listeners
   * @param configParser
   * @param log
   */
  protected static void notifyListeners(Set<ConfigurationListener<?>> listeners, ConfigurationParser configParser, Logger log) {
    if(!listeners.isEmpty()) {
      for(ConfigurationListener<?> listener : listeners) {
        if(listener instanceof ConfigurationLocationAware) {
          log.debug("Telling listener {} which directory the configuration is in {}", listener, configParser.getConfigurationDirectory());
          ConfigurationLocationAware locationAware = (ConfigurationLocationAware) listener;
          locationAware.setConfigurationDirectory(configParser.getConfigurationDirectory());
        }
        log.debug("Trying to notify listener {}", listener);
        Class<?> configClasses[] = getListenerGenericTypes(listener.getClass(), log);
        if(configClasses != null) {
          for(Class<?> type : configClasses) {
            CadmiumConfig cfgAnnotation = type.getAnnotation(CadmiumConfig.class);
            if(cfgAnnotation != null) {
              String key = type.getName();
              if(!StringUtils.isEmptyOrNull(cfgAnnotation.value())) {
                key = cfgAnnotation.value();
              }
              log.debug("Fetching configuration with key {}", key);
              try {
                Object cfg = configParser.getConfiguration(key, type);
                log.debug("Notifying listener {} with new configuration {}", listener, cfg);
                listener.configurationUpdated(cfg);
              } catch(ConfigurationNotFoundException e) {
                log.debug("Configuration not found. Notifying listener {}.", listener);
                listener.configurationNotFound();
              }
            } 
          }
        }
      }
    }
  }
  
  /**
   * Gets a list of classes that the listenerClass is interesting in listening to.
   * 
   * @param listenerClass
   * @param log
   * @return
   */
  private static Class<?>[] getListenerGenericTypes(Class<?> listenerClass, Logger log) {
    List<Class<?>> configClasses = new ArrayList<Class<?>>();
    Type[] typeVars = listenerClass.getGenericInterfaces();
    if(typeVars != null) {
      for(Type interfaceClass : typeVars) {
        if(interfaceClass instanceof ParameterizedType) {
          if(((ParameterizedType) interfaceClass).getRawType() instanceof Class) {
            if(ConfigurationListener.class.isAssignableFrom((Class<?>) ((ParameterizedType) interfaceClass).getRawType())) {
              ParameterizedType pType = (ParameterizedType) interfaceClass;
              Type[] typeArgs = pType.getActualTypeArguments();
              if(typeArgs != null && typeArgs.length == 1 && typeArgs[0] instanceof Class) {
                Class<?> type = (Class<?>) typeArgs[0];
                if(type.isAnnotationPresent(CadmiumConfig.class)){
                  log.debug("Adding "+type+" to the configuration types interesting to "+listenerClass);
                  configClasses.add(type);
                }
              }
            }
          }
        }
      }
    }
    return configClasses.toArray(new Class<?>[] {});
  }
  
  /**
   * Registers a {@link ConfigurationListener} instance with the current instance of ConfigurationManager.
   * @param listener
   */
  public void registerListener(ConfigurationListener<?> listener) {
    if(!listeners.contains(listener)) {
      listeners.add(listener);
      Set<ConfigurationListener<?>> newListener = new HashSet<ConfigurationListener<?>>();
      newListener.add(listener);
      notifyListeners(newListener, liveConfigParser, log);
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

  public String getWarFileName() {
    return warFileName;
  }

  public void setWarFileName(String warFileName) {
    this.warFileName = warFileName;
  }  

}
