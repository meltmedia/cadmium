package com.meltmedia.cadmium.servlets.jersey;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.github.ApiClient;

public class AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  protected String openId;
  protected ApiClient gitClient;
  
  protected boolean isAuth(String authString) {
    if(authString.toLowerCase().startsWith("token ")){
      authString = authString.substring(6).trim();
    }
    log.info("Authenticating request through github api with token [{}]", authString);
    try {
      gitClient = new ApiClient(authString);
      String env = System.getProperty("com.meltmedia.cadmium.environment", "dev");
      Properties teamsProps = new Properties();
      try {
        teamsProps.load(getClass().getClassLoader().getResourceAsStream("teams.properties"));
      } catch(Exception e){
        log.warn("Failed to load a team.properties file, skipping team based authentication.", e);
      }
      String defaultId = teamsProps.getProperty("default");
      String teamIdString = teamsProps.getProperty(env);
      if(teamIdString == null && defaultId == null) {
        openId = gitClient.getUserName();
        return true;
      } else if((teamIdString != null && isTeamMember(teamIdString)) || (defaultId != null && isTeamMember(defaultId))) {
        openId = gitClient.getUserName();
        return true;
      } else {
        gitClient = null;
      }
    } catch (Exception e) {
      log.warn("Failed to authenticate: "+authString, e);
    }
    return false;
  }
  
  private boolean isTeamMember(String ids) throws Exception {
    boolean inTeam = false;
    String idList[] = ids.split(",");
    for(String id : idList) {
      inTeam = gitClient.isTeamMember(id);
      if(inTeam) {
        break;
      }
    }
    return inTeam;
  }
}
