package com.meltmedia.cadmium.core.config;

import java.io.File;

/**
 * Adds a method to set the configuration directory for the current config update process.  
 * This must be used in addition to the ConfigurationListener interface.
 * 
 * @author John McEntire
 *
 */
public interface ConfigurationLocationAware {
  
  /**
   * Gets called with the current configuration directory that is being updated to.
   * 
   * @param configurationDirectory
   */
  public void setConfigurationDirectory(File configurationDirectory);
}
