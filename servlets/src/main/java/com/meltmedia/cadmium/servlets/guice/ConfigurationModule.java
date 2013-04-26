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
package com.meltmedia.cadmium.servlets.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.meltmedia.cadmium.core.CadmiumModule;
import com.meltmedia.cadmium.core.config.*;
import com.meltmedia.cadmium.core.config.impl.YamlConfigurationParser;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationCache;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Binds all {@link ConfigurationParser} related classes in Guice.
 * 
 * @author John McEntire
 *
 */
@CadmiumModule
public class ConfigurationModule extends AbstractModule {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @SuppressWarnings("rawtypes")
  @Override
  protected void configure() {
    Multibinder<Class> configurationClassBinder = Multibinder.newSetBinder(binder(), Class.class, ConfigurationClass.class);
    Reflections reflections = new Reflections("com.meltmedia.cadmium");
    Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(CadmiumConfig.class);
    log.debug("Found {} configuration classes", configClasses.size());
    for(Class<?> configClass : configClasses) {
      configurationClassBinder.addBinding().toInstance(configClass);
      log.debug("Adding configuration type {}", configClass.getName());
    }
    
    bind(Class.class).annotatedWith(ConfigurationParserClass.class).toInstance(YamlConfigurationParser.class);
    
    bind(ConfigurationParser.class).toProvider(ConfigurationParserProvider.class);
    
    bind(ConfigurationParserFactory.class);

    bind(AuthorizationCache.class);
  }

}
