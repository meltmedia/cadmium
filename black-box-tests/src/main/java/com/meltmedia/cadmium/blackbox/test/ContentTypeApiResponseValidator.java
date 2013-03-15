package com.meltmedia.cadmium.blackbox.test;

import org.apache.http.HttpResponse;

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
    String contentType = response.getFirstHeader("Content-Type").getValue();
    assertEquals("Content-Type {"+contentType+"} does not match the expected Content-Type: "+this.expectedContentType, this.expectedContentType, contentType);
  }
}
