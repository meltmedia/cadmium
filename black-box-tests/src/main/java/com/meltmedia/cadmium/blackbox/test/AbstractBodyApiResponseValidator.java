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
