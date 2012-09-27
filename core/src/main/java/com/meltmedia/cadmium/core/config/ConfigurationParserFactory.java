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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

/**
 * Simplifies the logic of getting a new instance of {@link ConfigurationParser} from the Guice Injector.
 * 
 * @author John McEntire
 *
 */
@Singleton
public class ConfigurationParserFactory {
  
  /**
   * The current instance of Guice Injector to get an instance of {@link ConfigurationParser} from.
   */
  @Inject
  protected Injector guiceInjector;
  
  /**
   * @return A new instance of a {@link ConfiguratonParser}.
   */
  public ConfigurationParser newInstance() {
    return guiceInjector.getInstance(ConfigurationParser.class);
  }
}
