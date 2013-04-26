package com.meltmedia.cadmium.servlets.jersey;

import com.meltmedia.cadmium.core.Scheduled;
import com.meltmedia.cadmium.core.github.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Holds and manages cache of github authorizations.
 */
@Singleton
public class AuthorizationCache {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private class Cache {
    Map<String, String> usernames = new HashMap<String, String>();
    Map<String, Integer[]> teamIds = new HashMap<String, Integer[]>();
    Map<String, Boolean> authorizations = new HashMap<String, Boolean>();
  }
  private Cache currentCache;

  public void checkToken(String token) throws Exception {
    Cache cache = currentCache;
    if(cache.authorizations.containsKey(token) && !cache.authorizations.get(token)) {
      throw new Exception("Token has been revoked.");
    } else if(!cache.authorizations.containsKey(token)){
      String username = fetchUsernameFromGithub(token);
      if(username == null) {
        cache.authorizations.put(token, false);
        throw new Exception("Token has been revoked.");
      } else {
        cache.usernames.put(token, username);
        cache.authorizations.put(token, true);
      }
    }
  }

  public Integer[] getTeamIds(String token) throws Exception {
    checkToken(token);
    Cache cache = currentCache;
    Integer teamIdList[] = cache.teamIds.get(token);
    if(teamIdList == null) {
      teamIdList = fetchTeamIdsFromGithub(token);
      if(teamIdList != null) {
        cache.teamIds.put(token, teamIdList);
      }
    }
    return teamIdList;
  }

  public String getUserName(String token) throws Exception {
    checkToken(token);
    Cache cache = currentCache;
    return currentCache.usernames.get(token);
  }

  @Scheduled(interval = 1, unit = TimeUnit.HOURS)
  public void clearCache() {
    logger.debug("Resetting cache.");
    currentCache = new Cache();
  }

  private Integer[] fetchTeamIdsFromGithub(String token) {
    try {
      ApiClient apiClient = new ApiClient(token, false);
      String orgs[] = apiClient.getAuthorizedOrgs();
      Set<Integer> teams = new TreeSet<Integer>();
      if(orgs != null) {
        for(String org: orgs) {
          Integer teamsInOrg[] = apiClient.getAuthorizedTeamsInOrg(org);
          if(teamsInOrg != null) {
            teams.addAll(Arrays.asList(teamsInOrg));
          }
        }
      }
      return teams.toArray(new Integer[] {});
    } catch (Exception e) {
      logger.warn("Failed to fetch team ids.", e);
    }
    return null;
  }

  private String fetchUsernameFromGithub(String token) {
    try {
      ApiClient apiClient = new ApiClient(token, false);
      return apiClient.getUserName();
    } catch(Exception e) {
      logger.warn("Failed to get user name.", e);
    }
    return null;
  }
}
