package com.meltmedia.cadmium.copyright.service;

import java.io.File;

/**
 * Performs a task to a given resource.
 * 
 * @author John McEntire
 *
 */
public interface ResourceHandler {
  
  /**
   * Does something to the given file.
   * 
   * @param htmlFile The resource to act on.
   */
  public void handleFile(File htmlFile);
}
