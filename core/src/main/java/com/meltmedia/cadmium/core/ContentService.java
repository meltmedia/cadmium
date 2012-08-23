/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
