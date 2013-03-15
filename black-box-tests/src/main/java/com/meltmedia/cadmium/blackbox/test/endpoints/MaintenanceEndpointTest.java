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
import com.meltmedia.cadmium.blackbox.test.AbstractBodyApiResponseValidator;
import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import com.meltmedia.cadmium.blackbox.test.BasicStatusApiResponseValidator;
import com.meltmedia.cadmium.core.api.BasicApiResponse;
import com.meltmedia.cadmium.core.api.MaintenanceRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Endpoint test for the System rest api for enabling and disabling the maintenance mode.
 *
 * @author John McEntire
 */
public class MaintenanceEndpointTest extends AbstractEnpointTest {

  protected MaintenanceRequest.State maintenanceState;

  public MaintenanceEndpointTest(String token, MaintenanceRequest.State maintenanceState) {
    super(token);
    this.maintenanceState = maintenanceState;
    setupTest();
  }

  @Override
  public void preTest() throws Exception {
    int expectedStatus = (this.maintenanceState == MaintenanceRequest.State.ON ? HttpStatus.SC_OK : HttpStatus.SC_SERVICE_UNAVAILABLE);
    new BasicStatusApiResponseValidator(expectedStatus)
        .validate(new ApiRequest("http://localhost:8901/index.html", ApiRequest.Method.GET).makeRequest());
  }

  @Override
  public void postTest() throws Exception {
    int expectedStatus = (this.maintenanceState == MaintenanceRequest.State.ON ? HttpStatus.SC_SERVICE_UNAVAILABLE : HttpStatus.SC_OK);
    new BasicStatusApiResponseValidator(expectedStatus)
        .validate(new ApiRequest("http://localhost:8901/index.html", ApiRequest.Method.GET).makeRequest());
  }

  @Override
  public void setupTest() {
    MaintenanceRequest requestBody = new MaintenanceRequest();
    requestBody.setState(this.maintenanceState);
    requestBody.setComment("Test turning maintenance page " + this.maintenanceState);
    request = new ApiRequest("http://localhost:8901/system/maintenance", ApiRequest.Method.POST, authToken, requestBody, "application/json");
    validator = new AbstractBodyApiResponseValidator("application/json", HttpStatus.SC_OK) {
      @Override
      public void validateBody(HttpResponse response, String responseBody) throws Exception {
        BasicApiResponse resp = new Gson().fromJson(responseBody, BasicApiResponse.class);
        assertNotNull("Failed to parse response: "+responseBody, resp);
        assertNotNull("Message not in response.", resp.getMessage());
        assertEquals(resp.getMessage(), "ok");

      }
    };
  }
}
