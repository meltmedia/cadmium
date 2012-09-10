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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigurationParserProviderTest {
  
  private ConfigurationParserProvider provider;
  
  @BeforeClass
  public static void setupSystem() {
    System.setProperty("com.meltmedia.cadmium.environment", "testing");
  }
  
  @SuppressWarnings("rawtypes")
  @Before
  public void testProviderConstructor() throws Exception {
    provider = new ConfigurationParserProvider(ConfigurationParserImpl.class);
    provider.configurationClasses = new HashSet<Class>();
    provider.configurationClasses.add(DummyClass.class);
  }
  
  @Test
  public void testProviderGet() throws Exception {
    ConfigurationParser parser1 = provider.get();
    validateParser(parser1);
    ConfigurationParser parser2 = provider.get();
    validateParser(parser2);
    assertTrue("Provider provided exact same instance for 2 calls to get.", parser1 != parser2);
  }
  
  public void validateParser(ConfigurationParser parser) {
    assertNotNull("Provider provided null.", parser);
    assertTrue("Parser is not of the expected type.", parser instanceof ConfigurationParserImpl);
    ConfigurationParserImpl impl = (ConfigurationParserImpl) parser;
    assertNotNull("Configuration classes not wired in.", impl.configurationClasses);
    assertEquals("Parser passed a different instance of the configuration classes.", impl.configurationClasses, provider.configurationClasses);
    assertNotNull("Environment not wired in.", impl.environment);
    assertEquals("Wrong environment wired in.", impl.environment, "testing");
  }
  
  
  public static class DummyClass {}
  
  public static class ConfigurationParserImpl implements ConfigurationParser {
    
    @SuppressWarnings("rawtypes")
    Collection<Class> configurationClasses;
    String environment;

    @SuppressWarnings("rawtypes")
    @Override
    public void setConfigurationClasses(
        Collection<Class> configurationClasses) {
      this.configurationClasses = configurationClasses;
    }

    @Override
    public void setEnvironment(String environment) {
      this.environment = environment;
    }

    @Override
    public void parseDirectory(File configurationDirectory) throws Exception {
      
    }

    @Override
    public <T> T getConfiguration(String key, Class<T> type)
        throws ConfigurationNotFoundException {
      return null;
    }
    
  }

}
