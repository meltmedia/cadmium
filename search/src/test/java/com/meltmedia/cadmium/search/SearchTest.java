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
package com.meltmedia.cadmium.search;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.meltmedia.cadmium.core.FileSystemManager;

public class SearchTest {

  private SearchContentPreprocessor preprocessor;
  private SearchService service;
  
  @Before
  public void setupIndexes() throws Exception {
    preprocessor = new SearchContentPreprocessor();
    preprocessor.processFromDirectory("./target/test-classes/test-content/META-INF");
    service = new SearchService();
    service.setIndexSearchProvider(preprocessor);
    
    preprocessor.makeLive();
    
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testSearchNoResults() throws Exception {
    Map<String, Object> results = service.search("bad_query",null);
    
    assertTrue("Should not be empty", !results.isEmpty());
    assertTrue("Should be set", results.containsKey("number-hits"));
    assertTrue("Should be integer", results.get("number-hits") instanceof Integer);
    assertTrue("Should say 0", new Integer(0).equals(results.get("number-hits")));
    assertTrue("Should have results entry", results.containsKey("results"));
    assertTrue("Should have no result entries", ((List<Map<String, Object>>)results.get("results")).isEmpty());
  }
  
  @After
  public void finishUp() throws Exception {
    preprocessor.finalize();
    FileSystemManager.deleteDeep(preprocessor.getIndexDir().getAbsolutePath());
  }
}
