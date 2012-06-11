package com.meltmedia.cadmium.core.github;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.github.ApiClient.Authorization;

public class ApiClientTest {
  private static String username;
  private static String password;
  private static Authorization tokenAuth;
  
  @Before
  public void setupForTest() throws Exception {
    File cadmiumDir = new File(System.getProperty("user.home"),".cadmium");
    File tokenFile = new File(cadmiumDir, "github.token");
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
      scopes.add("repos");
      
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
    List<Integer> ids = ApiClient.getAuthorizationIds(username, password);
    
    assertTrue("No id returned", ids != null && ids.size() > 0);
    assertTrue("Current auth id not in list", ids.contains(tokenAuth.getId()));
  }
  
  @Test
  public void testGetRateLimitRemaining() throws Exception {
    int limit = ApiClient.getRateLimitRemain(tokenAuth.getToken());
    
    assertTrue("Limit not greater than 0", limit > 0);
  }
}
