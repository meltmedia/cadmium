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
package com.meltmedia.cadmium.blackbox.test.endpoints;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.blackbox.test.AbstractBodyApiResponseValidator;
import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for client api search endpoint.
 *
 * @author John McEntire
 */
public class SearchEndpointTest extends AbstractEnpointTest {
  @Override
  public void setupTest() {
    request = new ApiRequest("http://localhost:8901/api/search?query=cadmium", ApiRequest.Method.GET, getBasicAuthHeader("tester","tester",null));
    validator = new AbstractBodyApiResponseValidator("application/json", HttpStatus.SC_OK) {
      @Override
      public void validateBody(HttpResponse response, String responseBody) throws Exception {
        Map<String, Object> resultObj = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());
        assertNotNull("No search results parsed", resultObj);
        assertTrue("number-hits not in results", resultObj.containsKey("number-hits"));
        assertNotNull("No results in search response.", resultObj.containsKey("results"));
        assertTrue("number-hits is not the expected type.", resultObj.get("number-hits") instanceof Number);
        assertTrue("results is not the expected type.", resultObj.get("results") instanceof List);
        assertTrue("number-hits does not equal the size of results.", ((Number)resultObj.get("number-hits")).intValue() == ((List)resultObj.get("results")).size());
        List<Map<?,?>> results = (List<Map<?,?>>)resultObj.get("results");
        assertTrue("Not any results to validate.", results.size() > 0);
        for(Map<?,?> result : results) {
          assertTrue("Result contains no score", result.containsKey("score"));
          assertTrue("Result contains no title", result.containsKey("title"));
          assertTrue("Result contains no path", result.containsKey("path"));
          assertTrue("Result contains no excerpts", result.containsKey("excerpt"));
        }
      }
    };
  }
}
