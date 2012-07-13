package com.meltmedia.cadmium.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author John McEntire
 * @author Chris Haley
 * @author Christian Trimble
 *
 */
public interface ContentService {
  public void switchContent(Long requestTime);
  public String getContentRoot();
  
  /**
   * Returns the input stream for the file at path, or null if there is no file at that path.  All paths must start with the '/' character
   * and will be resolved relative to the content root.
   * 
   * @param path
   * @return
   * @throws IOException
   */
  public InputStream getResourceAsStream( String path )
    throws IOException;
  
}
