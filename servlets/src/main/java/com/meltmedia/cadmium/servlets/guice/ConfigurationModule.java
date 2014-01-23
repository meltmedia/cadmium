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
import com.meltmedia.cadmium.core.config.CadmiumConfig;
import com.meltmedia.cadmium.core.config.ConfigurationClass;
import com.meltmedia.cadmium.core.config.ConfigurationParser;
import com.meltmedia.cadmium.core.config.ConfigurationParserClass;
import com.meltmedia.cadmium.core.config.impl.YamlConfigurationParser;
import com.meltmedia.cadmium.servlets.jersey.ApiService;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationCache;
import com.meltmedia.cadmium.servlets.jersey.github.GithubApiService;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Binds all {@link  ConfigurationParser} related classes in Guice.
 * 
 * @author John McEntire
 *
 */
public class ConfigurationModule extends AbstractModule {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private Reflections reflections;

  public ConfigurationModule(Reflections reflections) {
    this.reflections = reflections;
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected void configure() {
    Multibinder<Class> configurationClassBinder = Multibinder.newSetBinder(binder(), Class.class, ConfigurationClass.class);
    Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(CadmiumConfig.class);
    log.debug("Found {} configuration classes", configClasses.size());
    for(Class<?> configClass : configClasses) {
      configurationClassBinder.addBinding().toInstance(configClass);
      log.debug("Adding configuration type {}", configClass.getName());
    }
    
    bind(Class.class).annotatedWith(ConfigurationParserClass.class).toInstance(YamlConfigurationParser.class);
    
    bind(com.meltmedia.cadmium.core.config.ConfigurationParser.class).toProvider(com.meltmedia.cadmium.core.config.ConfigurationParserProvider.class);
    
    bind(com.meltmedia.cadmium.core.config.ConfigurationParserFactory.class);

    bind(ApiService.class).to(GithubApiService.class);

    bind(AuthorizationCache.class);
  }

}
