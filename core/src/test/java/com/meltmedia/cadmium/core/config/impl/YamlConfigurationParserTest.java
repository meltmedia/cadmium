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
package com.meltmedia.cadmium.core.config.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is a test of the {@link YamlConfigurationParser}.
 * 
 * @author John McEntire
 *
 */
public class YamlConfigurationParserTest {

  @Test
  public void testParseDirectory() throws Exception {
    YamlConfigurationParser parser = new YamlConfigurationParser();
    parser.setConfigurationClasses(Arrays.asList(new Class[] {TestConfigPojo.class}));
    parser.setEnvironment("production");
    parser.parseDirectory(new File("src/test/resources/test-configurations"));
    
    assertTrue("No configuration was parsed.", !parser.configuration.isEmpty());
    assertTrue("No default env was found.", parser.configuration.containsKey(YamlConfigurationParser.DEFAULT));
    Map<String, ?> defaultSection = parser.configuration.get(YamlConfigurationParser.DEFAULT);
    assertTrue("Wrong number of configurations found in default env.", defaultSection.size() == 2);
    validateConfig(defaultSection, "test", "test", "value", 15);
    validateConfig(defaultSection, "test2", "test2", "another value", 17);
    
    assertTrue("No production env was found.", parser.configuration.containsKey("production"));
    Map<String, ?> prodSection = parser.configuration.get("production");
    assertTrue("Wrong number of configurations found in production env.", prodSection.size() == 3);
    validateConfig(prodSection, "test", "updated value", "value", 15);
    validateConfig(prodSection, "test2", "updated value 2", "another value", 17);
    validateConfig(prodSection, "test3", "test 3", "yet another value", 99);
    
    
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testGetConfiguration() throws Exception {
    YamlConfigurationParser parser = new YamlConfigurationParser();
    parser.setConfigurationClasses(Arrays.asList(new Class[] {TestConfigPojo.class}));
    parser.setEnvironment("production");
    
    parser.configuration.put(YamlConfigurationParser.DEFAULT, new HashMap<String, Object>());
    parser.configuration.put("production", new HashMap<String, Object>());
    
    TestConfigPojo defaultTest = new TestConfigPojo();
    defaultTest.setName("test");
    defaultTest.setField("field");
    defaultTest.setAnotherField(55);
    ((Map<String, Object>)parser.configuration.get(YamlConfigurationParser.DEFAULT)).put("test", defaultTest);
    
    TestConfigPojo prodTest = new TestConfigPojo();
    prodTest.setName("test2");
    prodTest.setField("field2");
    prodTest.setAnotherField(66);
    ((Map<String, Object>)parser.configuration.get("production")).put("test", prodTest);
    
    assertTrue("Got wrong instance [production]", parser.getConfiguration("test", TestConfigPojo.class) == prodTest);
    parser.environment = "development";
    assertTrue("Got wrong instance [development]", parser.getConfiguration("test", TestConfigPojo.class) == defaultTest);
    
  }

  private void validateConfig(Map<String, ?> defaultSection, String key, String name, String field, Integer anotherField) {
    assertTrue("No "+key+" section was found in default environment.", defaultSection.containsKey(key));
    assertTrue(key+" section was not correct type.", defaultSection.get(key) instanceof TestConfigPojo);
    TestConfigPojo config = (TestConfigPojo) defaultSection.get(key);
    assertTrue("Incorrect name ["+config.getName()+"]", name.equals(config.getName()));
    assertTrue("Incorrect field ["+config.getField()+"]", field.equals(config.getField()));
    assertTrue("Incorrect anotherField ["+config.getAnotherField()+"]", anotherField.equals(config.getAnotherField()));
  }
}
