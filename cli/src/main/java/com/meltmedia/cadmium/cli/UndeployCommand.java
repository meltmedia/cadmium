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
package com.meltmedia.cadmium.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.api.UndeployRequest;

/**
 * Displays a list of Cadmium wars deployed to a JBoss server with a Cadmium-Deployer war deployed and tells it to undeploy any of them.
 * 
 * @author John McEntire
 *
 */
@Parameters(commandDescription="Undeploys a cadmium war", separators="=")
public class UndeployCommand extends AbstractAuthorizedOnly implements
    CliCommand {

  private static final Logger log = LoggerFactory.getLogger(UndeployCommand.class);
  
  @Parameter(description="<site>", required=true)
  private List<String> args;

  @Override
  public void execute() throws Exception {
    if(args == null || args.size() != 1) {
      System.err.println("Please specify the url to a deployer.");
      System.exit(1);
    }
    try{
      String site = getSecureBaseUrl(args.get(0));
    List<String> deployed = getDeployed(site, token);
    if(deployed == null || deployed.isEmpty()) {
      System.out.println("There are no cadmium wars currently deployed.");
      return;
    }
    int selectedIndex = -1;
    while(selectedIndex < 0) {
      int index = 0;
      System.console().format("%5s |  %s\n", "index", "Cadmium App");
      for(String app : deployed) {
        System.console().format("%5d | \"%s\"\n", index++, app);
      }
      
      System.out.println("Enter index or x to exit: ");
      String selection = null;
      try {
        selection = System.console().readLine();
      } catch(Throwable e){}
      
      if(selection != null && "x".equalsIgnoreCase(selection)) {
        return;  
      } else if (selection != null && selection.matches("\\d+")) {
        int theIdx = Integer.parseInt(selection);
        if(theIdx >= 0 && theIdx < deployed.size()) {
          selectedIndex = theIdx;
        } else {
          System.err.println(theIdx + " is not a valid choice.");
        }
      } else {
        System.err.println(selection + " is not a valid choice.");
      }
    }
    
    String undeploy = deployed.get(selectedIndex);
    
    System.out.println("Undeploying "+undeploy+" from "+site);
    
    String domain = null;
    String context = null;
    if(undeploy.contains("/")) {
      domain = undeploy.substring(0, undeploy.indexOf('/'));
      if(undeploy.length() > undeploy.indexOf('/')) {
        context = undeploy.substring(undeploy.indexOf('/') + 1);
      } else {
        context = "";
      }
      undeploy(site, domain, context, token);
      
    } else {
      System.err.println("Invalid app name: "+undeploy);
      System.exit(1);
    }
    } catch(Throwable t){t.printStackTrace();}
  }

  @Override
  public String getCommandName() {
    return "undeploy";
  }
  
  /**
   * Retrieves a list of Cadmium wars that are deployed.
   *  
   * @param url The uri to a Cadmium deployer war.
   * @param token The Github API token used for authentication.
   * @return
   * @throws Exception
   */
  public static List<String> getDeployed(String url, String token) throws Exception {
    List<String> deployed = new ArrayList<String> ();
    DefaultHttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
    
    HttpGet get = new HttpGet(url + "/system/deployment/list");
    addAuthHeader(token, get);
    
    HttpResponse resp = client.execute(get);
    
    if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      List<String> respList = new Gson().fromJson(EntityUtils.toString(resp.getEntity()), new TypeToken<List<String>>() {}.getType());
      get.releaseConnection();
      if(respList != null) {
        deployed.addAll(respList);
      }
    }
    
    return deployed;
  }
  
  /**
   * Sends the undeploy command to a Cadmium-Deployer war.
   * 
   * @param url The uri to a Cadmium-Deployer war.
   * @param domain The domain to undeploy.
   * @param context The context to undeploy.
   * @param token The Github API token used for authentication.
   * @throws Exception
   */
  public static void undeploy(String url, String domain, String context, String token) throws Exception {
    DefaultHttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
    
    HttpPost del = new HttpPost(url + "/system/undeploy");
    addAuthHeader(token, del);
    del.addHeader("Content-Type", MediaType.APPLICATION_JSON);
    
    UndeployRequest req = new UndeployRequest();
    req.setDomain(domain);
    req.setContextRoot(context);
    
    del.setEntity(new StringEntity(new Gson().toJson(req), "UTF-8"));
    
    HttpResponse resp = client.execute(del);
    
    if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
      Header hdr = resp.getFirstHeader("Location");
      if(hdr != null) {
        log.debug("Location: {}", hdr.getValue());
        del.releaseConnection();
        waitForUndeployment(client, domain, hdr.getValue());
      } else {
        System.err.println("Error response: "+resp);
        throw new Exception("Error response: "+resp);
      }
    }
  }
  

  
  /**
   * Waits for undeployment to complete or fail.
   * 
   * @param client
   * @param site
   * @param repo
   * @param location
   */
  private static void waitForUndeployment(HttpClient client, String site, String location) {
    Map<String, Integer> lastLogIndexes = new HashMap<String, Integer>();
    long startTime = System.currentTimeMillis();
    long timeoutTime = startTime + (5 * 60 * 1000l);
    boolean finished = false;
    while(System.currentTimeMillis() < timeoutTime) {
      String response = null;
      HttpGet get = null;
      try {
        get = new HttpGet(location);
        HttpResponse resp = client.execute(get);
        int statusCode = resp.getStatusLine().getStatusCode();
        response = EntityUtils.toString(resp.getEntity());
        if(statusCode == HttpStatus.SC_ACCEPTED) {
          updateMessages(lastLogIndexes, new Gson().fromJson(response, DeploymentStatus.class));
          Thread.sleep(5000l);
        } else if(statusCode == HttpStatus.SC_OK) {
          System.out.println("Successfully undeployed cadmium application to [" + site + "]");
          finished = true;
          break;
        } else if(statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
          System.err.println("Failed to undeploy to [" + site + "]:\n" + response);
          System.exit(1);
        }
      } catch(Throwable t) {
        throw new RuntimeException("Failed to check status of undeployment["+response+"].", t);
      } finally {
        if(get != null) {
          get.releaseConnection();
        }
      }
    }
    if(!finished) {
      System.err.println("Timed out waiting for undeployment!");
    }
    
  }

  /**
   * Displays any new messages that have been replied with.
   * 
   * @param lastLogIndexes
   * @param status
   */
  private static void updateMessages(Map<String, Integer> lastLogIndexes,
      DeploymentStatus status) {
    if(status != null && status.memberLogs != null) {
      for(String member : status.memberLogs.keySet()) {
        int i = getLastIndex(member, lastLogIndexes);
        List<String> memLogs = status.memberLogs.get(member);
        if(memLogs.size() > i) {
          for(; i < memLogs.size(); i = incLastIndex(member, lastLogIndexes, i)) {
            System.out.println("["+member+"] "+memLogs.get(i));
          }
        }
      }
    }
  }

  private static int incLastIndex(String member,
      Map<String, Integer> lastLogIndexes, int i) {
    i++;
    lastLogIndexes.put(member, i);
    return i;
  }

  /**
   * @param member
   * @param lastLogIndexes
   * @return Gets the last log index for the given member.
   */
  private static int getLastIndex(String member,
      Map<String, Integer> lastLogIndexes) {
    if(lastLogIndexes.containsKey(member)) {
      return lastLogIndexes.get(member);
    }
    return 0;
  }

}
