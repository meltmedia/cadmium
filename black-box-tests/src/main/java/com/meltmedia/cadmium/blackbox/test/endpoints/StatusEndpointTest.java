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
import com.meltmedia.cadmium.status.Status;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        assertNotNull("Status members not present.", statusResponse.getMembers());
        assertTrue("Status members not present.", statusResponse.getMembers().size() == 1);
        assertNotNull("Status warInfo not present.", statusResponse.getMembers().get(0).getWarInfo());
        assertEquals("Configuration branch not setup as expected.", statusResponse.getMembers().get(0).getWarInfo().getConfigBranch(), "cfg-master");
        assertEquals("Content branch not setup as expected.", statusResponse.getMembers().get(0).getWarInfo().getContentBranch(), "cd-master");
      }
    };
  }
}
