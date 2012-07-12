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
    Map<String, Object> results = service.search("bad_query");
    
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
