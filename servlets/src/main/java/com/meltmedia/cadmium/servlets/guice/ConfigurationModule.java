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

import java.util.Set;

import org.reflections.Reflections;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.meltmedia.cadmium.core.CadmiumModule;
import com.meltmedia.cadmium.core.config.CadmiumConfig;
import com.meltmedia.cadmium.core.config.ConfigurationClass;
import com.meltmedia.cadmium.core.config.ConfigurationParser;
import com.meltmedia.cadmium.core.config.ConfigurationParserClass;
import com.meltmedia.cadmium.core.config.ConfigurationParserFactory;
import com.meltmedia.cadmium.core.config.ConfigurationParserProvider;
import com.meltmedia.cadmium.core.config.impl.YamlConfigurationParser;

/**
 * Binds all {@link ConfigurationParser} related classes in Guice.
 * 
 * @author John McEntire
 *
 */
@CadmiumModule
public class ConfigurationModule extends AbstractModule {

  @SuppressWarnings("rawtypes")
  @Override
  protected void configure() {
    Multibinder<Class> configurationClassBinder = Multibinder.newSetBinder(binder(), Class.class, ConfigurationClass.class);
    Reflections reflections = new Reflections("com.meltmedia.cadmium");
    Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(CadmiumConfig.class);
    for(Class<?> configClass : configClasses) {
      configurationClassBinder.addBinding().toInstance(configClass);
    }
    
    bind(Class.class).annotatedWith(ConfigurationParserClass.class).toInstance(YamlConfigurationParser.class);
    
    bind(ConfigurationParser.class).toProvider(ConfigurationParserProvider.class);
    
    bind(ConfigurationParserFactory.class);
  }

}
