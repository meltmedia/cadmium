package com.meltmedia.cadmium.blackbox.test.endpoints;

import com.meltmedia.cadmium.blackbox.test.AbstractBodyApiResponseValidator;
import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Endpoint test for the System status health rest endpoint.
 *
 * @author John McEntire
 */
public class StatusHealthEndpointTest extends AbstractEnpointTest {
  @Override
  public void setupTest() {
    request = new ApiRequest("http://localhost:8901/system/status/health", ApiRequest.Method.GET);
    validator = new AbstractBodyApiResponseValidator("text/plain", HttpStatus.SC_OK) {
      @Override
      public void validateBody(HttpResponse response, String responseBody) throws Exception {
        Yaml parser = new Yaml();
        Iterable<Object> itr = parser.loadAll(responseBody);
        boolean foundOne = false;
        boolean foundTooMany = false;
        for (Object obj : itr) {
          if (!foundOne) {
            foundOne = true;
          } else if (!foundTooMany) {
            foundTooMany = true;
            break;
          }
          assertTrue("Yaml object {" + obj.getClass().getName() + "} not parsed into a Map", obj instanceof Map);
          assertTrue("No data parsed.", !((Map<?, ?>) obj).isEmpty());
        }
        assertTrue("Response {" + responseBody + "} did not parse as expected.", foundOne && !foundTooMany);
      }
    };
  }
}
