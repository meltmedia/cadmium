package com.meltmedia.cadmium.core.config;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;

/**
 * This implements the PropertiesWriter Interface.  For use in the ConfigManager class 
 * 
 * @author Brian Barr
 */

public interface PropertiesWriter {

  public void persistProperties(Properties properties, File propsFile, String message, Logger log);
  
}
