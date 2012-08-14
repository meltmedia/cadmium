package com.meltmedia.cadmium.core.config.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.meltmedia.cadmium.core.config.PropertiesReader;

public class PropertiesReaderImpl implements PropertiesReader {

  @Override
  public Properties getProperties(String fileName, Logger log) {

    Properties properties = new Properties();

    if(new File(fileName).canRead()) {

      FileInputStream in = null;
      try {

        in = new FileInputStream(fileName);        
        properties.load(in);
      }
      catch(Exception e){

        log.warn("Failed to read in properties file.", e);
      } 
      finally {
        IOUtils.closeQuietly(in);
      }
    }

    return properties;
  }

  @Override
  public Properties getProperties(File file, Logger log) {

    Properties properties = new Properties();    
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

    return properties;
  }

  @Override
  public Properties getProperties(ServletContext context, String path, Logger log) {

    Properties properties = new Properties();
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

    return properties;
  }

  @Override
  public Properties getProperties(Properties properties, String path, Logger log) {

    FileReader reader = null;
    try {

      reader = new FileReader(path);
      properties.load(reader);
    }
    catch(Exception e) {

      log.warn("Failed to load in properties for path: {}", path);
    }
    finally {
      IOUtils.closeQuietly(reader);
    }  

    return properties;
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

  @Override
  public Properties appendProperties(Properties properties, File configFile, Logger log) {

    if( !configFile.exists() /*|| !configFile.canRead()*/) return properties;

    Reader reader = null;
    try{

      log.info("configFile path: {}", configFile.getPath());

      reader = new FileReader(configFile);
      properties.load(reader);

      logProperties(log, properties, configFile.getCanonicalPath());
    }
    catch(Exception e) {

      log.warn("Failed to load properties file ["
          + configFile.getAbsolutePath() + "] from content directory.", e);
    }
    finally {

      IOUtils.closeQuietly(reader);
    }

    return properties;
  }

}
