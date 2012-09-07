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

import java.lang.reflect.Constructor;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice Provider that will create new instances of the {@link ConfigurationParser} that will be provided through a Guice Injector.
 * 
 * @author John McEntire
 *
 */
@Singleton
public class ConfigurationParserProvider implements Provider<ConfigurationParser> {
  
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The Guice Multibindings of Class Objects wired to Annotation {@link ConfigurationClass}.
   */
  @SuppressWarnings("rawtypes")
  @Inject
  @ConfigurationClass
  protected Set<Class> configurationClasses;
  
  /**
   * The zero argument constructor of the ConfigurationParser implementation used by this provider.
   */
  private Constructor<? extends ConfigurationParser> parserConstructor;
  
  /**
   * Constructs a new instance of this class by getting the zero argument constructor of the Class passed in.
   * 
   * @param parserType This must not be null.
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Inject
  public ConfigurationParserProvider(@ConfigurationParserClass Class parserType) throws SecurityException, NoSuchMethodException {
    if(ConfigurationParser.class.isAssignableFrom(parserType)) {
      parserConstructor = (Constructor<? extends ConfigurationParser>) parserType.getConstructor();
    } else {
      throw new IllegalArgumentException("Class passed in must be a sub type of ConfigurationParser.");
    }
  }

  /**
   * Factory method to create new instances of the configured implementation of {@link ConfigurationParser}.
   */
  @Override
  public ConfigurationParser get() {
    try {
      ConfigurationParser parser = parserConstructor.newInstance();
      parser.setConfigurationClasses(configurationClasses);
      parser.setEnvironment(System.getProperty("com.meltmedia.cadmium.environment"));
      return parser;
    } catch (Exception e) {
      logger.error("Failed to create a new ConfigurationParser Instance.", e);
      throw new RuntimeException("Failed to create a new ConfigurationParser Instance.");
    }
  }

}
