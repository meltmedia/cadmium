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

import java.util.Properties;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.github.ApiClient;

public class AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  protected String openId;
  protected ApiClient gitClient;
  
  @Inject
  protected ConfigManager configManager;
  
  protected boolean isAuth(String authString) {
    if(authString.toLowerCase().startsWith("token ")){
      authString = authString.substring(6).trim();
    }
    log.info("Authenticating request through github api with token [{}]", authString);
    
    Properties systemProperties = configManager.getSystemProperties();
    
    try {
      gitClient = new ApiClient(authString);
          
      String env = systemProperties.getProperty("com.meltmedia.cadmium.environment", "dev");
           
      String teamsFile = systemProperties.getProperty("com.meltmedia.cadmium.teams.properties");
      Properties teamsProps = configManager.getPropertiesByFileName(teamsFile);
           
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
