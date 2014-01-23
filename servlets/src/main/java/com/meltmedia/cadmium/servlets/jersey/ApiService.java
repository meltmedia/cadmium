package com.meltmedia.cadmium.servlets.jersey;

import com.meltmedia.cadmium.core.AuthorizationApi;

/**
 * com.meltmedia.cadmium.servlets.jersey.ApiService
 *
 * @author jmcentire
 */
public interface ApiService {
  public AuthorizationApi getAuthorizationApi(String token) throws Exception;
}
