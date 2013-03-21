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
import com.meltmedia.cadmium.blackbox.test.GitBareRepoInitializer;
import com.meltmedia.cadmium.core.api.BasicApiResponse;
import com.meltmedia.cadmium.core.api.UpdateRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import java.io.File;

import static com.meltmedia.cadmium.blackbox.test.CadmiumAssertions.assertContentDeployed;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests the content update rest endpoint.
 *
 * @author John McEntire
 */
public class UpdateEndpointTest extends AbstractEnpointTest {

  protected final String TEST_CONTENT_LOCATION = "target/filtered-resources/test-content";
  protected final String TEST_UPDATED_CONTENT_LOCATION = "target/filtered-resources/test-content-update";
  protected static final String DEPLOY_URL = "http://localhost:8901";
  protected GitBareRepoInitializer git;
  protected UpdateRequest updateRequest = new UpdateRequest();
  protected final BasicApiResponse apiResponse = new BasicApiResponse();

  public UpdateEndpointTest(String token, GitBareRepoInitializer git) {
    super(token);
    this.git = git;
    request.setPostBody(updateRequest);
  }

  @Override
  public void preTest() throws Exception {
    try {
      assertContentDeployed("Initial content isn't as expected.", new File(TEST_CONTENT_LOCATION), DEPLOY_URL, "tester", "tester");
      String revision = git.setupContentUpdate(TEST_UPDATED_CONTENT_LOCATION);
      updateRequest.setComment("Updating for test.");
      updateRequest.setBranch("cd-master");
      updateRequest.setRepo(git.getRepo());
      updateRequest.setSha(revision);
      System.out.println("Passed pretest...");
    } catch (AssertionError e) {
      fail("Pretest failed: " + e.getMessage());
    }
  }

  @Override
  public void setupTest() {
    request = new ApiRequest(DEPLOY_URL + "/system/update", ApiRequest.Method.POST, authToken, updateRequest, "application/json");
    validator = new BodyValidator("application/json", HttpStatus.SC_OK);
  }

  @Override
  public void postTest() throws Exception {
    try {
      waitForDeployment();
      assertContentDeployed("Failed to update content.", new File(TEST_UPDATED_CONTENT_LOCATION), DEPLOY_URL, "tester", "tester");
      System.out.println("Passed post test...");
    } catch (AssertionError e) {
      fail("Post test failed: " + e.getMessage());
    }
  }

  protected void waitForDeployment() throws Exception {
    ApiRequest req = new ApiRequest(DEPLOY_URL+"/system/history/"+apiResponse.getUuid(), ApiRequest.Method.GET);
    int numTries = 120;
    while(numTries > 0) {
      HttpResponse response = req.makeRequest();
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        String responseBody = EntityUtils.toString(response.getEntity());
        if(responseBody.equals("true")) {
          break;
        }
      } else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR){
        fail("Update failed.");
      }
      Thread.sleep(1000l);
      numTries--;
    }
    if(numTries == 0) {
      fail("Update took too long.");
    }
  }

  protected class BodyValidator extends AbstractBodyApiResponseValidator {

    public BodyValidator(String contentType, int status) {
      super(contentType, status);
    }

    @Override
    public void validateBody(HttpResponse response, String responseBody) throws Exception {
      BasicApiResponse updateResponse = null;
      try {
        updateResponse = new Gson().fromJson(responseBody, BasicApiResponse.class);
      } catch (Exception e) {
        fail("Failed to parse update response: " + e.getMessage());
      }
      assertNotNull("Response not parsed.", updateResponse);
      assertNotNull("No uuid", updateResponse.getUuid());
      assertEquals("update failed.", updateResponse.getMessage(), "ok");
      apiResponse.setTimestamp(updateResponse.getTimestamp());
      apiResponse.setUuid(updateResponse.getUuid());
    }
  }
}
