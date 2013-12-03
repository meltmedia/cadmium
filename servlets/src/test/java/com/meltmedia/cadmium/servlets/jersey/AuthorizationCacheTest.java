package com.meltmedia.cadmium.servlets.jersey;

import com.meltmedia.cadmium.core.AuthorizationApi;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationCache.Cache;
import org.junit.Test;
import org.slf4j.Logger;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * com.meltmedia.cadmium.servlets.jersey.AuthorizationCacheTest
 *
 * @author jmcentire
 */
public class AuthorizationCacheTest {
  private static final String TOKEN = "token";
  private ApiService apiService = null;
  private AuthorizationApi authApi = null;

  public AuthorizationCache setupCache(String token, Boolean authorizedInCache, Boolean authorized, String username, Integer[] teamIdsInCache, String[] orgs, Integer[][] teamIds) {
    apiService = mock(ApiService.class);
    authApi = mock(AuthorizationApi.class);
    if(authorized != null) {
      if(!authorized) {
        try {
          doThrow(new Exception("")).when(authApi).getUserName();
        } catch (Exception e){}
      } else {
        try {
          doReturn(username).when(authApi).getUserName();
          if(orgs != null && orgs.length > 0) {
            doReturn(orgs).when(authApi).getAuthorizedOrgs();
            if(teamIds != null && teamIds.length == orgs.length) {
              for(int i=0; i<orgs.length ; i++) {
                when(authApi.getAuthorizedTeamsInOrg(eq(orgs[i]))).thenReturn(teamIds[i]);
              }
            }
          }
        } catch(Exception e){}
      }
    }
    try {
      when(apiService.getAuthorizationApi(eq(token))).thenReturn(authApi);
    } catch(Exception e){}
    Cache cache = new Cache();
    if(authorizedInCache != null) {
      cache.authorizations.put(token, authorizedInCache);
      cache.usernames.put(token, username);
      if(teamIdsInCache != null) {
        cache.teamIds.put(token, teamIdsInCache);
      }
    }

    AuthorizationCache authCache = new AuthorizationCache();
    authCache.logger = mock(Logger.class);
    authCache.apiService = apiService;
    authCache.currentCache = cache;
    return authCache;
  }

  @Test
  public void checkTokenGoodInCacheTest() throws Exception {
    AuthorizationCache cache = setupCache(TOKEN, true, null, "test user", null, null, null);
    cache.checkToken(TOKEN);
    verifyZeroInteractions(apiService, authApi);
  }

  @Test
  public void checkTokenBadInCacheButGoodTest() throws Exception {
    AuthorizationCache cache = setupCache(TOKEN, false, true, "test user", null, null, null);
    cache.checkToken(TOKEN);
    verify(apiService, times(1)).getAuthorizationApi(eq(TOKEN));
    verify(authApi, times(1)).getUserName();
    verifyNoMoreInteractions(apiService, authApi);
  }

  @Test(expected = Exception.class)
  public void checkTokenBadInCacheButBadTest() throws Exception {
    AuthorizationCache cache = setupCache(TOKEN, false, true, null, null, null, null);
    cache.checkToken(TOKEN);
  }

  @Test
  public void checkTokenNotInCacheButGoodTest() throws Exception {
    AuthorizationCache cache = setupCache(TOKEN, null, true, "test user", null, null, null);
    cache.checkToken(TOKEN);
    verify(apiService, times(1)).getAuthorizationApi(eq(TOKEN));
    verify(authApi, times(1)).getUserName();
    verifyNoMoreInteractions(apiService, authApi);
  }

  @Test(expected = Exception.class)
  public void checkTokenNotInCacheButBadTest() throws Exception {
    AuthorizationCache cache = setupCache(TOKEN, null, true, null, null, null, null);
    cache.checkToken(TOKEN);
  }
}
