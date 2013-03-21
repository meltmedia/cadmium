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
package com.meltmedia.cadmium.blackbox.test;

import org.apache.http.HttpResponse;

import static org.junit.Assert.assertTrue;

/**
 * ApiResponseValidator that simply checks the response status code with an expected status code.
 *
 * @author John McEntire
 */
public class BasicStatusApiResponseValidator implements ApiResponseValidator {

  protected int expectedStatusCode;

  public BasicStatusApiResponseValidator(int expectedStatusCode) {
    this.expectedStatusCode = expectedStatusCode;
  }

  @Override
  public void validate(HttpResponse response) {
    int statusCode = response.getStatusLine().getStatusCode();
    assertTrue("Status code {"+statusCode+"} did not match the expected value: "+expectedStatusCode, statusCode == expectedStatusCode);
  }
}
