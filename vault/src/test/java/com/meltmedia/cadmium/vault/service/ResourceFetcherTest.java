package com.meltmedia.cadmium.vault.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.meltmedia.cadmium.vault.service.ResourceFetcher;

public class ResourceFetcherTest {
  private static final String VAULT_BASE_URL = "http://vault.meltmedia.com";
  private static final String RESOURCE_GUID = "0eab62ce-5131-4419-8e11-c986e0993545";
  
  @Test
  public void testFetch() throws Exception {
    ResourceFetcher fetcher = new ResourceFetcher(VAULT_BASE_URL);
    
    String content = fetcher.fetch(RESOURCE_GUID, null);
    
    assertTrue("Resource didn't fetch.", content != null && content.trim().length() > 0);
  }
}
