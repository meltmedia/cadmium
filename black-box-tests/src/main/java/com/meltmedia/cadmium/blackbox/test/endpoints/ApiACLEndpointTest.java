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
import com.meltmedia.cadmium.blackbox.test.ApiResponseValidator;
import com.meltmedia.cadmium.blackbox.test.BasicStatusApiResponseValidator;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Tests the system disabled rest endpoints.
 *
 * @author John McEntire
 */
public class ApiACLEndpointTest extends AbstractEnpointTest {
  protected static final String endpointUrl = "http://localhost:8901/system/disabled";
  protected static final String testEndpoint = "testing/" + UUID.randomUUID();
  protected ApiRequest listEndpoint;
  protected static final ApiResponseValidator endPointValidator = new BasicStatusApiResponseValidator(HttpStatus.SC_NO_CONTENT);

  public ApiACLEndpointTest(String token) {
    super(token);
    listEndpoint = new ApiRequest(endpointUrl, ApiRequest.Method.GET, authToken);
  }

  @Override
  public void preTest() throws Exception {
    checkEndpointEnabled();
  }

  @Override
  public void setupTest() {
    request = new ApiRequest(endpointUrl+"/"+testEndpoint, ApiRequest.Method.PUT, authToken);
    validator = new BasicStatusApiResponseValidator(HttpStatus.SC_NO_CONTENT);
  }

  @Override
  public void postTest() throws Exception {
    checkEndpointDisabled();
    new BasicStatusApiResponseValidator(HttpStatus.SC_NO_CONTENT).validate(new ApiRequest(endpointUrl+"/"+testEndpoint, ApiRequest.Method.DELETE, authToken).makeRequest());
    checkEndpointEnabled();
  }

  public void checkEndpointEnabled() throws Exception {
    new CheckDisabledValidator(false).validate(listEndpoint.makeRequest());
  }

  public void checkEndpointDisabled() throws Exception {
    new CheckDisabledValidator(true).validate(listEndpoint.makeRequest());
  }

  private class CheckDisabledValidator extends AbstractBodyApiResponseValidator {

    private boolean shouldHaveBeenDisabled = false;

    public CheckDisabledValidator(boolean shouldHaveBeenDisabled) {
      super("application/json", HttpStatus.SC_OK);
      this.shouldHaveBeenDisabled = shouldHaveBeenDisabled;
    }

    @Override
    public void validateBody(HttpResponse response, String responseBody) throws Exception {
      List<String> disabledPaths = new Gson().fromJson(responseBody, new TypeToken<List<String>>() {}.getType());
      assertNotNull("Failed to parse disabled paths.", disabledPaths);
      if(this.shouldHaveBeenDisabled) {
        assertTrue("Endpoint "+testEndpoint+" is not in the disabled paths list.", disabledPaths.contains(testEndpoint));
      } else {
        assertTrue("Endpoint " + testEndpoint + " is in the disabled paths list.", !disabledPaths.contains(testEndpoint));
      }
    }
  }
}
