package com.meltmedia.cadmium.core.config;

import java.util.Properties;

import org.slf4j.Logger;

public interface PropertiesWriter {

  public void persistProperties(Properties properties, String fileName, String message, Logger log);
  
}
