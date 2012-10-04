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

/**
 * An interface for any class that is interested in configuration 
 * updates for specific configuration types.
 * 
 * @author John McEntire
 *
 * @param <T> The type of the configuration Object.
 */
public interface ConfigurationListener<T> {
  
  /**
   * Called whenever a configuration of type T is updated.
   * 
   * @param configuration
   */
  public void configurationUpdated(Object configuration);
  
  /**
   * Called when the configuration is not found in an update.
   */
  public void configurationNotFound();
}
