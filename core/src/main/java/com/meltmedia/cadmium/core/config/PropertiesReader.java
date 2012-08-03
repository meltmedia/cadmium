package com.meltmedia.cadmium.core.config;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

public interface PropertiesReader {

  public Properties getProperties(String fileName);
  public Properties getProperties(ServletContext context, String path);
  
}
