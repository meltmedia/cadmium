package com.meltmedia.cadmium.core.config.impl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.meltmedia.cadmium.core.config.PropertiesReader;

/**
 * This implements the PropertiesReader Interface.  For use in the ConfigManager class 
 * 
 * @author Brian Barr
 */

public class PropertiesReaderImpl implements PropertiesReader {
  
  @Override
  public Properties getProperties(File file, Logger log) {

    Properties properties = new Properties();    
    readPropertiesWithReader(properties, file, log);
    return properties;
  }

  @Override
  public Properties getProperties(ServletContext context, String path, Logger log) {

    Properties properties = new Properties();    
    readPropertiesWithContext(properties, context, path, log);
    return properties;
  }

  @Override
  public Properties getProperties(Properties properties, String path, Logger log) {

    File file = new File(path);
    readPropertiesWithReader(properties, file, log);
    return properties;
  }  

  @Override
  public Properties appendProperties(Properties properties, File configFile, Logger log) {

    readPropertiesWithReader(properties, configFile, log);
    return properties;
  }

  private void readPropertiesWithReader(Properties properties, File file, Logger log) {

    Reader reader = null;
    try{

      log.info("configFile path: {}", file.getPath());
      reader = new FileReader(file);
      properties.load(reader);     

      logProperties(log, properties, file.getCanonicalPath());
    }
    catch(Exception e) {

      log.warn("Failed to load "+file.getAbsolutePath());
    }
    finally {

      IOUtils.closeQuietly(reader);
    }

  }

  private void readPropertiesWithContext(Properties properties, ServletContext context, String path, Logger log) {

    Reader reader = null;
    try{

      reader = new InputStreamReader(context.getResourceAsStream(path), "UTF-8");
      properties.load(reader);

      logProperties(log, properties, path);
    } 
    catch(Exception e) {

      log.warn("Failed to load "+path);
    }
    finally {

      IOUtils.closeQuietly(reader);
    }

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

}
