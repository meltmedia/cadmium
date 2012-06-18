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
