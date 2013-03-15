package com.meltmedia.cadmium.blackbox.test.endpoints;

import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import com.meltmedia.cadmium.blackbox.test.ApiResponseValidator;

/**
 * Interface for a individual rest endpoint test to run in the junit test case RestApiTest.
 */
public interface EndpointTest {
  public ApiRequest getRequest();
  public ApiResponseValidator getValidator();
  public void preTest() throws Exception;
  public void postTest() throws Exception;
}
