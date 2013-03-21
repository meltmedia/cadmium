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
import org.eclipse.jgit.util.StringUtils;

import static org.junit.Assert.assertEquals;

/**
 * ApiResponseValidator that simply matches the response content type header with an expected one.
 *
 * @author John McEntire
 */
public class ContentTypeApiResponseValidator extends BasicStatusApiResponseValidator {
  protected String expectedContentType;

  public ContentTypeApiResponseValidator(String expectedContentType, int expectedStatusCode) {
    super(expectedStatusCode);
    this.expectedContentType = expectedContentType;
  }

  @Override
  public void validate(HttpResponse response) {
    if(expectedStatusCode != -1) {
      super.validate(response);
    }
    if(!StringUtils.isEmptyOrNull(expectedContentType)) {
      String contentType = response.getFirstHeader("Content-Type").getValue();
      assertEquals("Content-Type {"+contentType+"} does not match the expected Content-Type: "+this.expectedContentType, this.expectedContentType, contentType);
    }
  }
}
