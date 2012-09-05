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
package com.meltmedia.cadmium.core.config;

import java.io.File;

import javassist.NotFoundException;

/**
 * <p>An interface of a Configuration Parser that will be created for each configuration update. This will be run in the following steps:<br />
 *   <ol>
 *     <li>The implementation of this interface will be initialized with a List of Class Objects. (<em>The Class Objects passed in will be wired via Guice and must be Pojo's annotated with {@link CadmiumConfig}.</em>)</li>
 *     <li>The {@link ConfigurationParser#parseDirectory(File)} method will be called with a File Object that will point to the directory that holds the configuration files.</li>
 *     <li>When ever a module needs access to a configuration the call will be delegated to the {@link ConfigurationParser#getConfiguration(String, Class)} method.</li> 
 *   </ol>
 * </p>
 * 
 * @author John McEntire
 *
 */
public interface ConfigurationParser {
  
  /**
   * This method will process all configuration files that are contained in the directory specified.
   * 
   * @param configurationDirectory A File Object that references the directory where all configuration file will be stored.
   * @throws Exception 
   */
  public void parseDirectory(File configurationDirectory) throws Exception;
  
  /**
   * This method will be used to fetch a configuration from the freshly parsed configurations.
   * 
   * @param key The key that references the configuration wanted.
   * @param type The type of the Pojo that the configuration was parsed in as.
   * @return The instance of the Pojo that is created off of the configuration parsed.
   * @throws NotFoundException Thrown if the configuration requested does not exist.
   */
  public <T> T getConfiguration(String key, Class<T> type) throws NotFoundException;
}
