package com.meltmedia.cadmium.servlets.jersey.github;

import com.meltmedia.cadmium.core.AuthorizationApi;
import com.meltmedia.cadmium.core.github.ApiClient;
import com.meltmedia.cadmium.servlets.jersey.ApiService;

/**
 * com.meltmedia.cadmium.servlets.jersey.github.GithubApiService
 *
 * @author jmcentire
 */
public class GithubApiService implements ApiService {

  @Override
  public AuthorizationApi getAuthorizationApi(String token) throws Exception {
    return new ApiClient(token, false);
  }
}
