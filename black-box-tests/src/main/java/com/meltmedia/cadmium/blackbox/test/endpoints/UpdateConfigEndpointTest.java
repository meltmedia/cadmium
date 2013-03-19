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
