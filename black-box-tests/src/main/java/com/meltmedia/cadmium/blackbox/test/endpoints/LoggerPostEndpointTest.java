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
 * Tests the system logger post endpoint.
 *
 * @author John McEntire
 */
public class LoggerPostEndpointTest extends AbstractEnpointTest {

  public LoggerPostEndpointTest(String token) {
    super(token);
  }

  @Override
  public void setupTest() {
    request = new ApiRequest("http://localhost:8901/system/logger/WARN", ApiRequest.Method.POST, authToken);
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
