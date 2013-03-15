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
