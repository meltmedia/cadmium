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
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.blackbox.test.AbstractBodyApiResponseValidator;
import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import com.meltmedia.cadmium.blackbox.test.BasicStatusApiResponseValidator;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for system auth rest api endpoint.
 *
 * @author John McEntire
 */
public class AuthenticationManagerEndpointTest extends AbstractEnpointTest {

  protected static final String TEST_PAGE = "http://localhost:8901/index.html";
  protected static final String ENDPOINT_URL = "http://localhost:8901/system/auth";
  protected static final String TEST_USERNAME = UUID.randomUUID().toString();
  protected static final String TEST_PASSWORD_ENC = "$shiro1$SHA-256$500000$mSdYLGFgcYvWReNVrh40Fg==$pkWR9MJ+niIdHMyqoXfLzl8t35miE0TviSHGPZVY5w0=";
  protected static final String TEST_PASSWORD = "test";

  public AuthenticationManagerEndpointTest(String token) {
    super(token);
  }

  @Override
  public void preTest() throws Exception {
    validateAuthList(false);
    checkStatus(HttpStatus.SC_UNAUTHORIZED, TEST_USERNAME, TEST_PASSWORD);
  }

  @Override
  public void setupTest() {
    request = new ApiRequest(ENDPOINT_URL + "/" + TEST_USERNAME, ApiRequest.Method.PUT, authToken, TEST_PASSWORD_ENC, "text/plain");
    validator = new BasicStatusApiResponseValidator(HttpStatus.SC_CREATED) {
      @Override
      public void validate(HttpResponse response) {
        super.validate(response);
        Header locationHeader = response.getFirstHeader("Location");
        assertNotNull("Location header not set.", locationHeader);
        assertTrue("Location header not set to the correct value.", locationHeader.getValue().contains(TEST_USERNAME));
      }
    };
  }

  @Override
  public void postTest() throws Exception {
    validateAuthList(true);
    checkStatus(HttpStatus.SC_OK, TEST_USERNAME, TEST_PASSWORD);
    removeUser();
    validateAuthList(false);
    checkStatus(HttpStatus.SC_UNAUTHORIZED, TEST_USERNAME, TEST_PASSWORD);
  }

  protected void removeUser() throws Exception {
    new BasicStatusApiResponseValidator(HttpStatus.SC_GONE).validate(new ApiRequest(ENDPOINT_URL + "/" + TEST_USERNAME, ApiRequest.Method.DELETE, authToken).makeRequest());
  }

  protected void validateAuthList(final boolean contains) throws Exception {
    new AbstractBodyApiResponseValidator("application/json", HttpStatus.SC_OK) {

      @Override
      public void validateBody(HttpResponse response, String responseBody) throws Exception {
        List<String> authList = new Gson().fromJson(responseBody, new TypeToken<List<String>>(){}.getType());
        assertNotNull("Auth list not parsed.", authList);
        if(contains) {
          assertTrue("Auth list does not contain new username.", authList.contains(TEST_USERNAME));
        } else {
          assertTrue("Auth list does contain new username.", !authList.contains(TEST_USERNAME));
        }
      }

    }.validate(new ApiRequest(ENDPOINT_URL, ApiRequest.Method.GET, authToken).makeRequest());
  }

  protected void checkStatus(int status, String username, String password) throws Exception {
    Map<String, String> authHeader = getBasicAuthHeader(username, password, null);
    new BasicStatusApiResponseValidator(status).validate(new ApiRequest(TEST_PAGE, ApiRequest.Method.GET, authHeader).makeRequest());
  }
}
