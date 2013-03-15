package com.meltmedia.cadmium.blackbox.test;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * ApiResponseValidator that simply matches the response body with an expected response string.
 *
 * @author John McEntire
 */
public class BasicBodyApiResponseValidator extends ContentTypeApiResponseValidator {
  protected String expectedBodyContent;

  public BasicBodyApiResponseValidator(String expectedBodyContent, String expectedContentType, int expectedStatusCode) {
    super(expectedContentType, expectedStatusCode);
    this.expectedBodyContent = expectedBodyContent;
  }

  @Override
  public void validate(HttpResponse response) {
    super.validate(response);
    try {
      String content = EntityUtils.toString(response.getEntity());
      assertEquals("Response Body {"+content+"} does not match the expected body content: "+this.expectedBodyContent, content, this.expectedBodyContent);
    } catch(IOException e) {
      fail("Failed with exception: "+e.getMessage());
    }
  }
}
