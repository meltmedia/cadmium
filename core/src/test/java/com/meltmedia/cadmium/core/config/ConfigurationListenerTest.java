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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ConfigurationListenerTest {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @SuppressWarnings("unchecked")
  @Test
  public void testNotifyListeners() throws Exception {
    Set<ConfigurationListener<?>> listeners = new HashSet<ConfigurationListener<?>>();
    Test1Listener listener1 = new Test1Listener();
    Test2Listener listener2 = new Test2Listener();
    listeners.add(listener1);
    listeners.add(listener2);
    Configuration config = new Configuration();
    ConfigurationParser parser = mock(ConfigurationParser.class);
    when(parser.getConfiguration("test1", Configuration.class)).thenReturn(config);
    when(parser.getConfiguration("test2", Configuration2.class)).thenThrow(ConfigurationNotFoundException.class);
    
    ConfigManager.notifyListeners(listeners, parser, log);
    
    assertTrue("Listener 1 not updated", listener1.updated);
    assertTrue("Listener 1 not found called.", !listener1.notfound);
    assertTrue("Listener 2 found and shouldn't have been.", listener2.notfound);
    assertTrue("Listener 2 should not have been updated.", !listener2.updated);
  }
  
  @CadmiumConfig("test1")
  class Configuration {
    public Configuration() {}
  }
  
  class Test1Listener implements ConfigurationListener<Configuration> {

    boolean updated = false;
    boolean notfound = false;
    
    @Override
    public void configurationUpdated(Object configuration) {updated = true;}

    @Override
    public void configurationNotFound() {notfound = true;}
    
  }
  
  @CadmiumConfig("test2") 
  class Configuration2 {
    public Configuration2() {}
  }
  
  class Test2Listener implements ConfigurationListener<Configuration2> {

    boolean updated = false;
    boolean notfound = false;
    
    @Override
    public void configurationUpdated(Object configuration) {updated = true;}

    @Override
    public void configurationNotFound() {notfound = true;}
    
  }
  
  
}
