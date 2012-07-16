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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.meltmedia.cadmium.core.FileSystemManager;

@RunWith(Parameterized.class)
public class SearchWithResultsTest {
  
  private static SearchContentPreprocessor preprocessor;
  private static SearchService service;
  private List<String> paths;
  private String query;

  public SearchWithResultsTest(String query, List<String> expectedResults) {
    this.paths = expectedResults;
    this.query = query;
  }

  
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { "meltmedia",  makeList("/index.html", "/other.html", "/subdir/index.html", "/subdir/test2.htm")},
        { "bar", makeList("/other.html") },
        { "good", makeList("/subdir/index.html") },
        { "title", makeList("/other.html", "/subdir/test2.htm") }});
  }
  
  private static List<String> makeList(String... strings) {
    return Arrays.asList(strings);
  }
  
  @BeforeClass
  public static void setupIndexes() throws Exception {
    preprocessor = new SearchContentPreprocessor();
    preprocessor.processFromDirectory("./target/test-classes/test-content/META-INF");
    service = new SearchService();
    service.setIndexSearchProvider(preprocessor);
    
    preprocessor.makeLive();
    
  }
  
  @AfterClass
  public static void finishUp() throws Exception {
    preprocessor.finalize();
    FileSystemManager.deleteDeep(preprocessor.getIndexDir().getAbsolutePath());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test() throws Exception {
    Map<String, Object> allResults = service.search(query);
    
    assertTrue("Should not be empty", !allResults.isEmpty());
    assertTrue("Should be set", allResults.containsKey("number-hits"));
    assertTrue("Should be integer", allResults.get("number-hits") instanceof Integer);
    assertTrue("Should say "+paths.size()+": "+allResults.get("number-hits"), new Integer(paths.size()).equals(allResults.get("number-hits")));
    assertTrue("Should have results entry", allResults.containsKey("results"));
    assertTrue("Should have "+paths.size()+" result entries", ((List<Map<String, Object>>)allResults.get("results")).size() == paths.size());
    List<Map<String, Object>> resultList = ((List<Map<String, Object>>)allResults.get("results"));
    List<String> found = new ArrayList<String>();
    for(int i=0; i<resultList.size(); i++) {
      Map<String, Object> result = resultList.get(i);
      assertTrue("Score isn't set", result.containsKey("score"));
      assertTrue("Path isn't set", result.containsKey("path"));
      assertTrue("Path not expected {"+result.get("path")+"}", paths.contains(result.get("path")));
      assertTrue("Path not unique: "+result.get("path"), !found.contains(result.get("path")));
      found.add((String)result.get("path"));
    }
  }

}
