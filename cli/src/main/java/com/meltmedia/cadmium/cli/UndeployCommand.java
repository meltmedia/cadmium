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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.api.UndeployRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of Cadmium wars deployed to a JBoss server with a Cadmium-Deployer war deployed and tells it to undeploy any of them.
 * 
 * @author John McEntire
 *
 */
@Parameters(commandDescription="Undeploys a cadmium war", separators="=")
public class UndeployCommand extends AbstractAuthorizedOnly implements
    CliCommand {
  
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
    
    
    if(!undeploy.isEmpty()) {
      undeploy(site, undeploy, token);
      
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
   * @param warName The war to undeploy.
   * @param token The Github API token used for authentication.
   * @throws Exception
   */
  public static void undeploy(String url, String warName, String token) throws Exception {
    DefaultHttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
    
    HttpPost del = new HttpPost(url + "/system/undeploy");
    addAuthHeader(token, del);
    del.addHeader("Content-Type", MediaType.APPLICATION_JSON);
    
    UndeployRequest req = new UndeployRequest();
    req.setWarName(warName);
    
    del.setEntity(new StringEntity(new Gson().toJson(req), "UTF-8"));
    
    HttpResponse resp = client.execute(del);
    
    if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      String respStr = EntityUtils.toString(resp.getEntity());
      if(!respStr.equals("ok")) {
        throw new Exception("Failed to undeploy "+warName);
      } else {
        System.out.println("Undeployment of "+warName+" successful");
      }
    } else {
      System.err.println("Failed to undeploy "+warName);
      System.err.println(resp.getStatusLine().getStatusCode()+": "+EntityUtils.toString(resp.getEntity()));
    }
  }

}
