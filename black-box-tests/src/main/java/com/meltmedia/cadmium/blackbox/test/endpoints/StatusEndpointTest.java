package com.meltmedia.cadmium.blackbox.test.endpoints;

import com.google.gson.Gson;
import com.meltmedia.cadmium.blackbox.test.AbstractBodyApiResponseValidator;
import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import com.meltmedia.cadmium.status.Status;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import static org.junit.Assert.assertNotNull;

/**
 * Endpoint test for the System status rest endpoint.
 *
 * @author John McEntire
 */
public class StatusEndpointTest extends AbstractEnpointTest {

  public StatusEndpointTest(String token) {
    super(token);
  }

  @Override
  public void setupTest() {
    request = new ApiRequest("http://localhost:8901/system/status", ApiRequest.Method.GET, authToken);
    validator = new AbstractBodyApiResponseValidator("application/json", HttpStatus.SC_OK) {
      @Override
      public void validateBody(HttpResponse response, String responseBody) throws Exception {
        Status statusResponse = new Gson().fromJson(responseBody, Status.class);
        assertNotNull("Status returned null.", statusResponse);
        assertNotNull("Status (GroupName) not parsed correctly.", statusResponse.getGroupName());
        assertNotNull("Status (Environment) not parsed correctly.", statusResponse.getEnvironment());
        assertNotNull("Status (Repo) not parsed correctly.", statusResponse.getRepo());
        assertNotNull("Status (Branch) not parsed correctly.", statusResponse.getBranch());
        assertNotNull("Status (Revision) not parsed correctly.", statusResponse.getRevision());
      }
    };
  }
}
