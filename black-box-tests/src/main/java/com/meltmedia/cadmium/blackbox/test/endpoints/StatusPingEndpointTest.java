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
