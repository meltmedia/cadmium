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
