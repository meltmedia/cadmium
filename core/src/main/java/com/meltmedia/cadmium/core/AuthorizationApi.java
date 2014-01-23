package com.meltmedia.cadmium.core;

/**
 * com.meltmedia.cadmium.core.AuthorizationApi
 *
 * @author jmcentire
 */
public interface AuthorizationApi {
  public void checkAuth() throws Exception;
  public void deauthorizeToken(String username, String password, long authId) throws Exception;
  public String getUserName() throws Exception;
  public String[] getAuthorizedOrgs() throws Exception;
  public Integer[] getAuthorizedTeamsInOrg(String org) throws Exception;
}
