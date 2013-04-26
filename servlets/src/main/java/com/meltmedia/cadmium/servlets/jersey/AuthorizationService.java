/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.servlets.jersey;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.servlets.guice.CadmiumListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  protected String openId;
  protected String token;

  @Inject
  protected AuthorizationCache apiCache;
  
  @Inject
  protected ConfigManager configManager;
  
  protected boolean isAuth(String authString) {
    if(authString.toLowerCase().startsWith("token ")){
      authString = authString.substring(6).trim();
    }
    log.trace("Authenticating request through github api with token [{}]", authString);
    
    try {
      apiCache.checkToken(authString);
      token = authString;

      List<Integer> idList = Arrays.asList(apiCache.getTeamIds(authString));
      log.debug("Authorized teams for {}: {}", token, new Gson().toJson(idList));
      String authorizedTeams = getAuthorizedTeamsString();
      if(authorizedTeams == null) {
        openId = apiCache.getUserName(authString);
        return true;
      } else if(isTeamMember(authorizedTeams, idList)) {
        openId = apiCache.getUserName(authString);
        return true;
      }
    } catch (Exception e) {
      log.warn("Failed to authenticate: "+authString, e);
    }
    return false;
  }
  
  private boolean isTeamMember(String ids, List<Integer> currentIds) throws Exception {
    boolean inTeam = false;
    Integer idList[] = splitAsIntegers(ids);
    for(Integer id : idList) {
      if(inTeam = currentIds.contains(id)) {
        break;
      }
    }
    return inTeam;
  }

  protected Integer[] getTeamIds() throws Exception {
    return apiCache.getTeamIds(token);
  }

  private String getAuthorizedTeamsString() {
    Properties systemProperties = configManager.getSystemProperties();
    String env = systemProperties.getProperty("com.meltmedia.cadmium.environment", "development");

    String teamsFile = systemProperties.getProperty("com.meltmedia.cadmium.teams.properties", new File(configManager.getSystemProperties().getProperty(CadmiumListener.BASE_PATH_ENV), "team.properties").getAbsoluteFile().getAbsolutePath());
    Properties teamsProps = configManager.getProperties(new File(teamsFile));

    log.trace("teamsProps: {}", teamsProps);

    String defaultId = teamsProps.getProperty("default");
    String teamIdString = teamsProps.getProperty(env);
    return (teamIdString == null ? "" : teamIdString) + (StringUtils.isNotBlank(defaultId) ? (StringUtils.isNotBlank(teamIdString) ? "," : "") + defaultId : "");
  }

  protected Integer[] getAuthorizedTeams() {
    String authTeams = getAuthorizedTeamsString();
    return splitAsIntegers(authTeams);
  }

  private Integer[] splitAsIntegers(String authTeams) {
    String teams[] = authTeams == null ? new String[]{} : authTeams.split(",");
    List<Integer> teamIds = new ArrayList<Integer>();
    for(String teamId: teams) {
      teamIds.add(new Integer(teamId));
    }
    return teamIds.toArray(new Integer[]{});
  }
}
