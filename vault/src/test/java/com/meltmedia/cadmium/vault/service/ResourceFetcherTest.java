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
