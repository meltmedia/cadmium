package com.meltmedia.cadmium.core.config;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;

public interface PropertiesReader {

  public Properties getProperties(String fileName, Logger log);
  public Properties getProperties(File file, Logger log);
  public Properties getProperties(ServletContext context, String path, Logger log);
  public Properties getProperties(Properties properties, String path,  Logger log);
  public Properties appendProperties(Properties properties, File configFile, Logger log);
  
}
