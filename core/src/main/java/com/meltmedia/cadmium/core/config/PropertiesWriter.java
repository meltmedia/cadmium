package com.meltmedia.cadmium.core.config;

import java.util.Properties;

import org.slf4j.Logger;

/**
 * This implements the PropertiesWriter Interface.  For use in the ConfigManager class 
 * 
 * @author Brian Barr
 */

public interface PropertiesWriter {

  public void persistProperties(Properties properties, String fileName, String message, Logger log);
  
}
