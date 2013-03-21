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
import com.meltmedia.cadmium.blackbox.test.ApiResponseValidator;
import sun.misc.BASE64Encoder;

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

  @Override
  public String toString() {
    return getClass().getName();
  }

  protected Map<String, String> getBasicAuthHeader(String username, String password, Map<String, String> addToMap) {
    if(addToMap == null) {
      addToMap = new HashMap<String, String>();
    }
    addToMap.put("Authorization", "Basic " + new String(new BASE64Encoder().encode((username + ":" + password).getBytes())));
    return addToMap;
  }
}
