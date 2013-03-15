package com.meltmedia.cadmium.blackbox.test.endpoints;

import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import com.meltmedia.cadmium.blackbox.test.ApiResponseValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class implementation of a Endpoint test to run in junit test case RestApiTest.
 *
 * @author John McEntire
 */
public abstract class AbstractEnpointTest implements EndpointTest {

  protected ApiRequest request;
  protected ApiResponseValidator validator;
  protected Map<String, String> authToken;

  public AbstractEnpointTest() {
    setupTest();
  }

  public AbstractEnpointTest(String token) {
    authToken = new HashMap<String, String>();
    authToken.put("Authorization", "token " + token);
    setupTest();
  }

  public abstract void setupTest();

  @Override
  public ApiRequest getRequest() {
    return request;
  }

  @Override
  public ApiResponseValidator getValidator() {
    return validator;
  }

  @Override
  public void preTest() throws Exception {}

  @Override
  public void postTest() throws Exception {}
}
