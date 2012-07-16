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

import org.junit.Before;
import org.junit.Test;

public class VaultServiceTest {

  private DummyResourceManager resourceManager;
  private VaultService service;
  
  @Before
  public void constructorTest() throws Exception {
    resourceManager = new DummyResourceManager();
    service = new VaultService();
    service.resourceManager = resourceManager;
    
    assumeTrue(service.guidCache.isEmpty());
  }
  
  @Test
  public void testGetSafety() throws Exception {
    String resourceGuid = "This is a test guid";
    String response = service.getSafety(resourceGuid);
    
    assertTrue("Guid not returned", response != null && response.equals(resourceGuid));
    assertTrue("Guid cache not increased in size", service.guidCache.size() == 1);
    
    response = service.getSafety(resourceGuid);
    
    assertTrue("Guid not returned again", response != null && response.equals(resourceGuid));
    assertTrue("Guid cache increased in size", service.guidCache.size() == 1);
  }
}
