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
import com.meltmedia.cadmium.blackbox.test.GitBareRepoInitializer;

import static org.junit.Assert.fail;

/**
 * Tests the configuration update rest endpoint.
 *
 * @author John McEntire
 */
public class UpdateConfigEndpointTest extends UpdateEndpointTest {
  protected final String TEST_UPDATED_CONTENT_LOCATION = "target/filtered-resources/test-config-update";

  public UpdateConfigEndpointTest(String token, GitBareRepoInitializer git) {
    super(token, git);
  }

  @Override
  public void preTest() throws Exception {
    try {
      String revision = git.setupConfigUpdate(TEST_UPDATED_CONTENT_LOCATION);
      updateRequest.setComment("Updating for test.");
      updateRequest.setBranch("cfg-master");
      updateRequest.setRepo(git.getRepo());
      updateRequest.setSha(revision);
    } catch (AssertionError e) {
      fail("Pretest failed: " + e.getMessage());
    }
  }

  @Override
  public void setupTest() {
    super.setupTest();
    request = new ApiRequest(DEPLOY_URL + "/system/update/config", ApiRequest.Method.POST, authToken, updateRequest, "application/json");
  }

  @Override
  public void postTest() throws Exception {
    try {
      waitForDeployment();
    } catch (AssertionError e) {
      fail("Post test failed: " + e.getMessage());
    }
  }
}
