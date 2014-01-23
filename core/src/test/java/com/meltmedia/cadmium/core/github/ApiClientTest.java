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

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.github.ApiClient.Authorization;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class ApiClientTest {
  private static String username;
  private static String password;
  private static Authorization tokenAuth;
  
  @Before
  public void setupForTest() throws Exception {
    File cadmiumDir = new File(System.getProperty("user.home"),".cadmium");
    File tokenFile = new File(cadmiumDir, "github.token");
    if(tokenFile.exists()) {
      tokenFile.renameTo(new File(cadmiumDir, "github.token.old"));
    }
    if(!tokenFile.exists()) {
      System.out.println("Token file doesn't exist.");
      if(username == null || password == null) {
        username = System.getProperty("github.username");
        password = System.getProperty("github.password");
      }
      
      if(username == null || password == null) {
        System.err.println("Please set your github.username and github.password in the java system properties to run these tests.");
        System.err.println("Skipping test!");
        assumeTrue(false);
      }
       
      List<String> scopes = new ArrayList<String>();
      scopes.add("repo");
      
      tokenAuth = ApiClient.authorize(username, password, scopes);
      
      assertTrue("Authorization failed.", tokenAuth != null);
      
      FileSystemManager.writeStringToFile(cadmiumDir.getAbsolutePath(), "github.token", tokenAuth.getToken());
    }
  }
  
  @After
  public void destroyAfter() throws Exception {

    File cadmiumDir = new File(System.getProperty("user.home"),".cadmium");
    File tokenFile = new File(cadmiumDir, "github.token");
    if(tokenFile.exists()) {
      tokenFile.delete();
    }
    tokenFile = new File(cadmiumDir, "github.token.old");

    if(tokenFile.exists()) {
      tokenFile.renameTo(new File(cadmiumDir, "github.token"));
    }
    
    if(tokenAuth != null) {
      ApiClient client = new ApiClient(tokenAuth.getToken());
      client.deauthorizeToken(username, password, tokenAuth.getId());
      tokenAuth = null;
    }
  }
  
  @Test
  public void testGetUsername() throws Exception {
    ApiClient client = new ApiClient();
    String login = client.getUserName();
    
    assertTrue("Login is incorrect", login != null && login.equals(username));
  }
  
  @Test
  public void testGetAuthorizationIds() throws Exception {
    List<Long> ids = ApiClient.getAuthorizationIds(username, password);
    
    assertTrue("No id returned", ids != null && ids.size() > 0);
    assertTrue("Current auth id not in list", ids.contains(tokenAuth.getId()));
  }
  
  @Test
  public void testGetRateLimitRemaining() throws Exception {
    int limit = ApiClient.getRateLimitRemain(tokenAuth.getToken(), new DefaultHttpClient());
    
    assertTrue("Limit not greater than 0", limit > 0);
  }
  
  @Test
  public void testGetOrgRepo() throws Exception {
    String orgRepoHttps = ApiClient.getOrgRepo("https://github.com/meltmedia/test-content-repo.git");
    assertTrue("Bad Org/Repo for Https", orgRepoHttps != null && orgRepoHttps.equals("meltmedia/test-content-repo"));
    String orgRepoSsh  = ApiClient.getOrgRepo("git@github.com:meltmedia/test-content-repo.git");
    assertTrue("Bad Org/Repo for SSH", orgRepoSsh != null && orgRepoSsh.equals("meltmedia/test-content-repo"));
    String orgRepoReadOnly  = ApiClient.getOrgRepo("git://github.com/meltmedia/test-content-repo.git");
    assertTrue("Bad Org/Repo for Read Only", orgRepoReadOnly != null && orgRepoReadOnly.equals("meltmedia/test-content-repo"));
  }
  
  @Test
  public void testComments() throws Exception {
    ApiClient client = new ApiClient(tokenAuth.getToken());
    
    long id = client.commentOnCommit("https://github.com/meltmedia/test-content-repo.git", "ae457d2ea15f3ce7d0a53cc01d12d3bb5c971ddb", "This is a comment from the junit test case.");
    
    assertTrue("The id is invalid", id > 0);
  }
}
