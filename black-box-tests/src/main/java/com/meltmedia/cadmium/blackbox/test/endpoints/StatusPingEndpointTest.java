package com.meltmedia.cadmium.blackbox.test.endpoints;

import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import com.meltmedia.cadmium.blackbox.test.BasicBodyApiResponseValidator;
import org.apache.http.HttpStatus;

/**
 * Endpoint test for the System status Ping rest endpoint.
 *
 * @author John McEntire
 */
public class StatusPingEndpointTest extends AbstractEnpointTest {
  @Override
  public void setupTest() {
    request = new ApiRequest("http://localhost:8901/system/status/Ping", ApiRequest.Method.GET);
    validator = new BasicBodyApiResponseValidator("Ok", "text/plain", HttpStatus.SC_OK);
  }
}
