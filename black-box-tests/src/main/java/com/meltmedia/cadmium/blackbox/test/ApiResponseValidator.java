package com.meltmedia.cadmium.blackbox.test;

import org.apache.http.HttpResponse;

/**
 * Interface for all rest API Response validators.
 *
 * @author John McEntire
 */
public interface ApiResponseValidator {
  public void validate(HttpResponse response);
}
