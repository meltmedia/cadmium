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
package com.meltmedia.cadmium.core.meta;

import java.util.regex.Pattern;

/**
 * <p><code>META-INF/</code> configuration to map requests to specific alternate content directories.</p>
 * <p>Following is an example json config file that will be processed into a list of these objects:</p>
 * <pre>
 *   [
 *     {
 *       "pattern": ".*(mobile).*",
 *       "contentDirectory": "mobile"
 *     }
 *   ]
 * </pre>
 *
 * @author John McEntire
 */
public class AlternateContent {
  /**
   * The pattern that will be compiled and matched against the "User-Agent" from the incomming request.
   */
  private String pattern;

  /**
   * Precompiled pattern.
   */
  protected Pattern compiledPattern = null;

  /**
   * The content directory name that should be relative and will be forced to be under the META-INF directory of the content directory.
   */
  private String contentDirectory;

  public AlternateContent(){}

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public String getContentDirectory() {
    return contentDirectory;
  }

  public void setContentDirectory(String contentDirectory) {
    this.contentDirectory = contentDirectory;
  }
}
