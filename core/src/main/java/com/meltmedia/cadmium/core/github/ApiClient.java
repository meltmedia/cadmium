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
package com.meltmedia.cadmium.core.github;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.FileSystemManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class ApiClient {
  private static final Logger log = LoggerFactory.getLogger(ApiClient.class);
  private String token;
  private String username;

  public ApiClient(String token, boolean check) throws Exception {
    this.token = token;
    if(check) {
      checkAuth();
    }
  }
  
  public ApiClient(String token) throws Exception {
    this.token = token;
    
    checkAuth();
  }

  public void checkAuth() throws Exception {
    username = getUserName();
    if (username == null) {
      throw new Exception("Token has been revoked.");
    }
  }
  
  public ApiClient() throws Exception {
    String cadmiumToken = getToken();
    
    if(cadmiumToken != null) {
      this.token = cadmiumToken;
      
      String username = getUserName();
      if(username == null) {
        throw new Exception("Token has been revoked.");
      }
    } else {
      throw new Exception("No token for user has been found.");
    }
  }

  public static String getToken() throws Exception {
    String cadmiumToken = System.getProperty("user.home");
    if(cadmiumToken != null) {
      cadmiumToken = FileSystemManager.getChildDirectoryIfExists(cadmiumToken, ".cadmium");
      if(cadmiumToken != null) {
        cadmiumToken = FileSystemManager.getFileIfCanRead(cadmiumToken, "github.token");
        if(cadmiumToken != null) {
          cadmiumToken = FileSystemManager.getFileContents(cadmiumToken);
        }
      }
    }
    return cadmiumToken;
  }
  
  public static Authorization authorize(String username, String password, List<String> scopes) throws Exception {
    int limitRemain = getRateLimitRemain(null); 
    if(limitRemain > 0) {
      DefaultHttpClient client = new DefaultHttpClient();
      
      HttpPost post = new HttpPost("https://api.github.com/authorizations");
      setupBasicAuth(username, password, post);
      AuthBody body = new AuthBody();
      if(scopes != null) {
        body.scopes = scopes.toArray(new String[] {});
      }
      String bodySt = new Gson().toJson(body, AuthBody.class);
      log.trace("Loggin in with post body [{}]", bodySt);
      StringEntity postEntity = new StringEntity(bodySt);
      post.setEntity(postEntity);
      try {
        HttpResponse response = client.execute(post);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
          Authorization auth = new Gson().fromJson(EntityUtils.toString(response.getEntity()), Authorization.class);
          return auth;
        } else {
          String errResponse = EntityUtils.toString(response.getEntity());
          log.warn("Github auth failed: {}", errResponse);
          throw new Exception(errResponse);
        }
      } finally {
        post.releaseConnection();
      }
    } else {
      throw new Exception("Request is rate limited.");
    }
  }
  
  public static void authorizeAndCreateTokenFile(String username, String password, List<String> scopes) throws Exception {
    Authorization auth = authorize(username, password, scopes);
    if(auth != null && auth.getToken() != null) {
      String cadmiumToken = System.getProperty("user.home");
      if(cadmiumToken != null) {
        File cadmiumDir = new File(cadmiumToken, ".cadmium").getAbsoluteFile();
        if(cadmiumDir.exists() || cadmiumDir.mkdirs()) {
          FileSystemManager.writeStringToFile(cadmiumDir.getAbsolutePath(), "github.token", auth.getToken());
        }
      }
    }
  }
  
  public static List<Long> getAuthorizationIds(String username, String password) throws Exception {
    int limitRemain = getRateLimitRemain(null);
    List<Long> authIds = new ArrayList<Long>();
    if(limitRemain > 0) {
      DefaultHttpClient client = new DefaultHttpClient();
      
      HttpGet get = new HttpGet("https://api.github.com/authorizations");
      setupBasicAuth(username, password, get);
      try {
        HttpResponse response = client.execute(get);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          List<Map<String, Object>> auths = new Gson().fromJson(EntityUtils.toString(response.getEntity()), new TypeToken<List<Map<String, Object>>>() {}.getType());
          if(auths != null && auths.size() > 0) {
            for(Map<String, Object> auth : auths) {
              if(auth != null && auth.containsKey("id")) {
                Double id = (Double)auth.get("id");
                authIds.add(id.longValue());
              }
            }
          }
        } else {
          EntityUtils.consume(response.getEntity());
        }
      } finally {
        get.releaseConnection();
      }
    } else {
      throw new Exception("Request is rate limited.");
    }
    return authIds;
  }

  private static void setupBasicAuth(String username, String password, HttpMessage message) {
    message.addHeader("Authorization", "Basic "+Base64.encodeBytes(new String(username+":"+password).getBytes()));
  }
  
  public void deauthorizeToken(String username, String password, long authId) throws Exception {
    int limitRemain = getRateLimitRemain();
    if(limitRemain > 0) {
      DefaultHttpClient client = new DefaultHttpClient();
      
      HttpDelete delete = new HttpDelete("https://api.github.com/authorizations/"+authId);
      setupBasicAuth(username, password, delete);
      try {
        HttpResponse response = client.execute(delete);
        if(response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
          EntityUtils.consume(response.getEntity());
          throw new Exception("Failed to deauthorize token "+token);
        }
      } finally {
        delete.releaseConnection();
      }
    } else {
      throw new Exception("Request is rate limited.");
    }
  }
  
  public boolean isTeamMember(String teamId) throws Exception {
    int limitRemain = getRateLimitRemain();
    if(limitRemain > 0) {
      HttpClient client = new DefaultHttpClient();
      
      HttpGet get = new HttpGet("https://api.github.com/teams/"+teamId);
      addAuthHeader(get);
      HttpResponse response = null;
      try {
        response = client.execute(get);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          return true;
        }
      } finally {
        if(response != null) {
          EntityUtils.consume(response.getEntity());
        }
        get.releaseConnection();
      }
    } else {
      throw new Exception("Request is rate limited.");
    }
    return false;
  }
  
  public String getUserName() throws Exception {
    if(StringUtils.isNotBlank(username)) {
      return username;
    }
    int limitRemain = getRateLimitRemain();
    
    if(limitRemain > 0) {
      HttpClient client = new DefaultHttpClient();
      
      HttpGet get = new HttpGet("https://api.github.com/user");
      addAuthHeader(get);
      try {
        HttpResponse response = client.execute(get);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          String responseString = EntityUtils.toString(response.getEntity());

          Map<String, Object> responseObj = new Gson().fromJson(responseString, new TypeToken<Map<String, Object>>() {}.getType());

          if(responseObj.containsKey("login")) {
            username = (String) responseObj.get("login");
            return username;
          } else if(responseObj.containsKey("message")) {
            throw new Exception((String) responseObj.get("message"));
          }
        } else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
          EntityUtils.consume(response.getEntity());
          throw new Exception("Unauthorized");
        } else {
          EntityUtils.consume(response.getEntity());
        }
      } finally {
        get.releaseConnection();
      }
    } else {
      throw new Exception("Request is rate limited.");
    }
    return null;
  }
  
  public int getRateLimitRemain() throws Exception {
    return getRateLimitRemain(token);
  }
  
  public static int getRateLimitRemain(String token) throws Exception {
    HttpClient client = new DefaultHttpClient();
    
    HttpGet get = new HttpGet("https://api.github.com/rate_limit");
    addAuthHeader(get, token);
    HttpResponse response = null;
    try {
      response = client.execute(get);
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        String responseString = EntityUtils.toString(response.getEntity());

        RateLimitResponse responseObj = new Gson().fromJson(responseString, new TypeToken<RateLimitResponse>() {}.getType());
        if(responseObj.rate != null) {
          if(responseObj.rate.remaining != null) {
            if(responseObj.rate.remaining < 100) {
              log.warn("The remaining rate limit is {}", responseObj.rate.remaining);
            } else {
              log.trace("The remaining rate limit is {}", responseObj.rate.remaining);
            }
            return responseObj.rate.remaining;
          }
        }
      } else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED){
        EntityUtils.consume(response.getEntity());
        throw new Exception("Unauthorized!");
      } else {
        EntityUtils.consume(response.getEntity());
      }
    } finally {
      get.releaseConnection();
    }
    return -1;
  }

  public String[] getAuthorizedOrgs() throws Exception {
    HttpClient client = new DefaultHttpClient();

    HttpGet get = new HttpGet("https://api.github.com/user/orgs");
    addAuthHeader(get);
    Set<String> orgs = new TreeSet<String>();
    try {
      HttpResponse response = client.execute(get);
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        String responseStr = EntityUtils.toString(response.getEntity());
        List<Org> retOrgs = new Gson().fromJson(responseStr, new TypeToken<List<Org>>(){}.getType());
        for(Org org : retOrgs) {
          orgs.add(org.login);
        }
      } else {
        EntityUtils.consumeQuietly(response.getEntity());
      }
    } finally {
      get.releaseConnection();
    }
    return orgs.toArray(new String[]{});
  }

  public Integer[] getAuthorizedTeamsInOrg(String org) throws Exception {
    HttpClient client = new DefaultHttpClient();

    HttpGet get = new HttpGet("https://api.github.com/orgs/"+org+"/teams");
    addAuthHeader(get);
    Set<Integer> teamIds = new TreeSet<Integer>();
    try {
      HttpResponse response = client.execute(get);
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        String responseStr = EntityUtils.toString(response.getEntity());
        List<Team> teams = new Gson().fromJson(responseStr, new TypeToken<List<Team>>(){}.getType());
        for(Team team : teams) {
          teamIds.add(team.id);
        }
      } else {
        EntityUtils.consumeQuietly(response.getEntity());
      }
    } finally {
      get.releaseConnection();
    }
    return teamIds.toArray(new Integer[]{});
  }
  
  public long commentOnCommit(String repoUri, String sha, String comment) throws Exception {
    String orgRepo = getOrgRepo(repoUri);
    
    HttpClient client = new DefaultHttpClient();
    
    HttpPost post = new HttpPost("https://api.github.com/repos/" + orgRepo + "/commits/" + sha + "/comments");
    addAuthHeader(post);
    
    Comment commentBody = new Comment();
    commentBody.body = comment;
    
    String commentJsonString = new Gson().toJson(commentBody, Comment.class);
    log.trace("Setting a new comment [{}]", commentJsonString);
    post.setEntity(new StringEntity(commentJsonString));
    try {
      HttpResponse response = client.execute(post);
    
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
        String resp = EntityUtils.toString(response.getEntity());
        IdExtractor id = new Gson().fromJson(resp, IdExtractor.class);
        return id.id;
      } else {
        EntityUtils.consume(response.getEntity());
        throw new Exception("Failed to create new comment of commit ["+orgRepo+":"+sha+"]: "+response.getStatusLine().toString());
      }
    } finally {
      post.releaseConnection();
    }
  }

  public static String getOrgRepo(String repoUri) throws Exception {
    repoUri = repoUri.replace(".git", "");
    repoUri = repoUri.split(":")[1];
    String orgName = null;
    String repoName = null;
    String splitRepo[] = repoUri.split("/");
    for(int i=0; i<splitRepo.length; i++) {
      if(i > 0) {
        if(splitRepo[i-1].trim().length() > 0 && splitRepo[i].trim().length() > 0) {
          orgName = splitRepo[i-1];
          repoName = splitRepo[i];
        }
      }
    }
    if(orgName == null || repoName == null) {
      throw new Exception("Invalid repo uri");
    }
    return orgName + "/" + repoName;
  } 
  
  private void addAuthHeader(HttpMessage message) {
    addAuthHeader(message, token);
  }

  private static void addAuthHeader(HttpMessage message, String token) {
    if(token != null) {
      message.addHeader("Authorization", "token "+token);
    }
  }  
  
  private static class IdExtractor {
    Long id;
  }
  
  private static class Comment {
    @SuppressWarnings("unused")
    String body;
  }
  
  private static class Rate {
    Integer remaining;
  }
  
  private static class RateLimitResponse {
    Rate rate;
  }
  
  private static class AuthBody {
    @SuppressWarnings("unused")
    String scopes[];
  }
  
  public static class Authorization {
    protected Long id;
    protected String token;
    protected String scopes[];
    public Long getId() {
      return id;
    }
    public void setId(Long id) {
      this.id = id;
    }
    public String getToken() {
      return token;
    }
    public void setToken(String token) {
      this.token = token;
    }
    public String[] getScopes() {
      return scopes;
    }
    public void setScopes(String[] scopes) {
      this.scopes = scopes;
    }
  }

  public static class Org {
    Integer id;
    String login;
    String url;
    String avatar_url;
  }

  public static class Team {
    String url;
    String name;
    Integer id;
  }
  
}
