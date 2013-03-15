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
import org.apache.http.util.EntityUtils;

import static org.junit.Assert.fail;

/**
 * Abstract Validator for rest command response body.
 *
 * @author John McEntire
 */
public abstract class AbstractBodyApiResponseValidator extends ContentTypeApiResponseValidator {
  public AbstractBodyApiResponseValidator(String expectedContentType, int expectedStatusCode) {
    super(expectedContentType, expectedStatusCode);
  }

  @Override
  public void validate(HttpResponse response) {
    super.validate(response);
    try {
      validateBody(response, EntityUtils.toString(response.getEntity()));
    } catch(Exception e) {
      fail("Failed to validate body: "+e.getMessage());
    }
  }

  public abstract void validateBody(HttpResponse response, String responseBody) throws Exception ;
}
