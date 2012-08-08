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
package com.meltmedia.cadmium.vault.service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResourceManagerTest {

  private static final String guid = "Test-guid";
  private DummyVaultService service;
  private DummyVaultListener listener;
  private DummyResourceFetcher fetcher;
  private ResourceManager manager;
  
  @Before
  public void constructNewManager() throws Exception {
    new File("target/manager-test/cache-dir/"+guid+".res").delete();
    new File("target/manager-test/vault.properties").delete();
    listener = new DummyVaultListener();
    fetcher = new DummyResourceFetcher("");
    service = new DummyVaultService();
    service.fetcher = fetcher;
    service.listener = listener;
    manager = new ResourceManager(service, "target/manager-test/vault.properties", "target/manager-test/cache-dir");
    assumeTrue(manager.vaultProperties.isEmpty());
  }
  
  @After
  public void testPersistVaultProperties() throws Exception {
    manager.persistVaultProperties();
    assertTrue("", new File("target/manager-test/vault.properties").exists());
  }
  
  @Test
  public void testGetSafety() throws Exception {
    String responseGuid = manager.getSafety(guid);
    
    assertTrue("Guids not returned correctly", responseGuid != null && responseGuid.equals(guid));
    
    assertTrue("Access time not updated", manager.vaultProperties.containsKey(guid));
    
    assertTrue("Cache file not created", new File("target/manager-test/cache-dir/"+guid+".res").exists());
    
    assertTrue("Resource fetched from the fetcher.", fetcher.fetched);

    fetcher.fetched = false;
    manager.vaultProperties.clear();
    responseGuid = manager.getSafety(guid);
    
    assertTrue("Guids not returned correctly pass 2", responseGuid != null && responseGuid.equals(guid));
    
    assertTrue("Access time not updated pass 2", manager.vaultProperties.containsKey(guid));
    
    assertTrue("Cache file not created pass 2", new File("target/manager-test/cache-dir/"+guid+".res").exists());
    
    assertTrue("Resource shouldn't be fetched from the fetcher.", !fetcher.fetched);
  }
  
  @Test
  public void testUpdateSafety() throws Exception {
    String responseGuid = manager.getSafety(guid);
    fetcher.overrideGuid = "This is an overridden guid";
    String guids[] = manager.updateSafety();
    
    assertTrue("Guid not updated", guids != null && guids.length == 1 && guids[0].equals(guid));
    
    assertTrue("Access time not updated", manager.vaultProperties.containsKey(guid));
    
    assertTrue("Cache file not created", new File("target/manager-test/cache-dir/"+guid+".res").exists());

    fetcher.fetched = false;
    responseGuid = manager.getSafety(guid);
    
    assertTrue("Guids not returned correctly", responseGuid != null && responseGuid.equals(fetcher.overrideGuid));
    
    assertTrue("Access time not updated", manager.vaultProperties.containsKey(guid));
    
    assertTrue("Cache file not created", new File("target/manager-test/cache-dir/"+guid+".res").exists());
    
    
  }
}