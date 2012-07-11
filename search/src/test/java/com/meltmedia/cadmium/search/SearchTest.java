package com.meltmedia.cadmium.search;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.meltmedia.cadmium.core.FileSystemManager;

@Ignore
public class SearchTest {

  private SearchContentPreprocessor preprocessor;
  private SearchService service;
  private static final List<String> paths = Arrays.asList(new String[] {"index.html", "other.html", "subdir/index.html", "subdir/test2.htm"});
  
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
  public void testSearchWithResults() throws Exception {
    Map<String, Object> allResults = service.search("meltmedia");
    
    assertTrue("Should not be empty", !allResults.isEmpty());
    assertTrue("Should be set", allResults.containsKey("number-hits"));
    assertTrue("Should be integer", allResults.get("number-hits") instanceof Integer);
    assertTrue("Should say 4: "+allResults.get("number-hits"), new Integer(4).equals(allResults.get("number-hits")));
    assertTrue("Should have results entry", allResults.containsKey("results"));
    assertTrue("Should have 4 result entries", ((List<Map<String, Object>>)allResults.get("results")).size() == 4);
    List<Map<String, Object>> resultList = ((List<Map<String, Object>>)allResults.get("results"));
    List<String> found = new ArrayList<String>();
    for(int i=0; i<resultList.size(); i++) {
      Map<String, Object> result = resultList.get(i);
      assertTrue("Score isn't set", result.containsKey("score"));
      assertTrue("Path isn't set", result.containsKey("path"));
      assertTrue("Path not expected {"+result.get("path")+"}", paths.contains(result.get("path")));
      assertTrue("Path not unique", found.contains(result.get("path")));
      found.add((String)result.get("path"));
    }
    
    Map<String, Object> oneResultFromTitle = service.search("bar");
    
    assertTrue("Should not be empty", !oneResultFromTitle.isEmpty());
    assertTrue("Should be set", oneResultFromTitle.containsKey("number-hits"));
    assertTrue("Should be integer", oneResultFromTitle.get("number-hits") instanceof Integer);
    assertTrue("Should say 1", new Integer(1).equals(oneResultFromTitle.get("number-hits")));
    assertTrue("Should have results entry", oneResultFromTitle.containsKey("results"));
    assertTrue("Should have 1 result entries", ((List<Map<String, Object>>)oneResultFromTitle.get("results")).size() == 1);
    resultList = ((List<Map<String, Object>>)oneResultFromTitle.get("results"));
    found = new ArrayList<String>();
    for(int i=0; i<resultList.size(); i++) {
      Map<String, Object> result = resultList.get(i);
      assertTrue("Score isn't set", result.containsKey("score"));
      assertTrue("Path isn't set", result.containsKey("path"));
      assertTrue("Path not expected {"+result.get("path")+"}", "other.html".equals(result.get("path")));
      assertTrue("Path not unique", found.contains(result.get("path")));
      found.add((String)result.get("path"));
    }
    
    Map<String, Object> oneResultFromBody = service.search("good");
    
    assertTrue("Should not be empty", !oneResultFromBody.isEmpty());
    assertTrue("Should be set", oneResultFromBody.containsKey("number-hits"));
    assertTrue("Should be integer", oneResultFromBody.get("number-hits") instanceof Integer);
    assertTrue("Should say 1", new Integer(1).equals(oneResultFromBody.get("number-hits")));
    assertTrue("Should have results entry", oneResultFromBody.containsKey("results"));
    assertTrue("Should have 1 result entries", ((List<Map<String, Object>>)oneResultFromBody.get("results")).size() == 1);
    resultList = ((List<Map<String, Object>>)oneResultFromBody.get("results"));
    found = new ArrayList<String>();
    for(int i=0; i<resultList.size(); i++) {
      Map<String, Object> result = resultList.get(i);
      assertTrue("Score isn't set", result.containsKey("score"));
      assertTrue("Path isn't set", result.containsKey("path"));
      assertTrue("Path not expected {"+result.get("path")+"}", "subdir/index.html".equals(result.get("path")));
      assertTrue("Path not unique", found.contains(result.get("path")));
      found.add((String)result.get("path"));
    }
    
    Map<String, Object> twoResults = service.search("title");
    
    assertTrue("Should not be empty", !twoResults.isEmpty());
    assertTrue("Should be set", twoResults.containsKey("number-hits"));
    assertTrue("Should be integer", twoResults.get("number-hits") instanceof Integer);
    assertTrue("Should say 2", new Integer(2).equals(twoResults.get("number-hits")));
    assertTrue("Should have results entry", twoResults.containsKey("results"));
    assertTrue("Should have 2 result entries", ((List<Map<String, Object>>)twoResults.get("results")).size() == 2);
    resultList = ((List<Map<String, Object>>)twoResults.get("results"));
    List<String> two = Arrays.asList(new String[] {"other.html", "subdir/test2.htm"});
    found = new ArrayList<String>();
    for(int i=0; i<resultList.size(); i++) {
      Map<String, Object> result = resultList.get(i);
      assertTrue("Score isn't set", result.containsKey("score"));
      assertTrue("Path isn't set", result.containsKey("path"));
      assertTrue("Path not expected {"+result.get("path")+"}", two.contains(result.get("path")));
      assertTrue("Path not unique", found.contains(result.get("path")));
      found.add((String)result.get("path"));
    }
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
