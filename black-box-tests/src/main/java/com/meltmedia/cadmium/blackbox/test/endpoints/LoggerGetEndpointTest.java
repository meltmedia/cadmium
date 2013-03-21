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
import com.meltmedia.cadmium.core.LoggerServiceResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the system logger get endpoint.
 *
 * @author John McEntire
 */
public class LoggerGetEndpointTest extends AbstractEnpointTest {

  public LoggerGetEndpointTest(String token) {
    super(token);
  }

  @Override
  public void setupTest() {
    request = new ApiRequest("http://localhost:8901/system/logger", ApiRequest.Method.GET, authToken);
    validator = new AbstractBodyApiResponseValidator("application/json", HttpStatus.SC_OK) {
      @Override
      public void validateBody(HttpResponse response, String responseBody) throws Exception {
        LoggerServiceResponse resp = new Gson().fromJson(responseBody, LoggerServiceResponse.class);
        assertNotNull("Logger service response was not parsed.", resp);
        assertNotNull("The config element of the logger service response is null.", resp.getConfigs());
        assertTrue("The config element of the logger service response is empty.", !resp.getConfigs().isEmpty());
      }
    };
  }
}
