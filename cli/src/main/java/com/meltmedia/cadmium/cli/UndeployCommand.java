package com.meltmedia.cadmium.cli;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.api.UndeployRequest;

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
    List<String> deployed = getDeployed(args.get(0), token);
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
    
    System.out.println("Undeploying "+undeploy+" from "+args.get(0));
    
    String domain = null;
    String context = null;
    if(undeploy.contains("/")) {
      domain = undeploy.substring(0, undeploy.indexOf('/'));
      if(undeploy.length() > undeploy.indexOf('/')) {
        context = undeploy.substring(undeploy.indexOf('/') + 1);
      } else {
        context = "";
      }
      undeploy(args.get(0), domain, context, token);
      
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
  
  public static List<String> getDeployed(String url, String token) throws Exception {
    List<String> deployed = new ArrayList<String> ();
    DefaultHttpClient client = new DefaultHttpClient();
    
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
  
  public static void undeploy(String url, String domain, String context, String token) throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    
    HttpPost del = new HttpPost(url + "/system/undeploy");
    addAuthHeader(token, del);
    del.addHeader("Content-Type", MediaType.APPLICATION_JSON);
    
    UndeployRequest req = new UndeployRequest();
    req.setDomain(domain);
    req.setContextRoot(context);
    
    del.setEntity(new StringEntity(new Gson().toJson(req), "UTF-8"));
    
    HttpResponse resp = client.execute(del);
    
    if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      String respStr = EntityUtils.toString(resp.getEntity());
      if(!respStr.equals("ok")) {
        throw new Exception("Failed to undeploy "+domain+"/"+context);
      } else {
        System.out.println("Undeployment of "+domain+"/"+context+" successful");
      }
    }
  }

}
