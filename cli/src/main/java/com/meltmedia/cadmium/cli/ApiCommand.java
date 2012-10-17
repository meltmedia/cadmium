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
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;

/**
 * Enables/Disables endpoints on the jersey "/api" endpoints in cadmium or lists currently disabled endpoints.
 * 
 * @author John McEntire
 *
 */
@Parameters(commandDescription="Manages the open api endpoints availability.", separators="=")
public class ApiCommand extends AbstractAuthorizedOnly implements CliCommand {
  private static final String DISABLE_ENDPOINT = "/system/api/disable";
  private static final String ENABLE_ENDPOINT = "/system/api/enable";
  private static final String LIST_ENDPOINT = "/system/api/disable/list";
  private enum OPERATION { DISABLE, ENABLE, LIST; }
  
  @Parameter(description = "<site> (list|[disable|enable <endpoint>*])", required = true)
  private List<String> params;

  @Override
  public String getCommandName() {
    return "api";
  }

  @Override
  public void execute() throws Exception {
    if(params.isEmpty()) {
      throw new Exception("Invalid usage.");
    }
    
    String site = getSecureBaseUrl(params.get(0));
    OPERATION op = OPERATION.LIST;
    List<String> paths = new ArrayList<String>();
    if(params.size() >= 2) {
      op = OPERATION.valueOf(params.get(1).toUpperCase());
      if(params.size() == 2 && ( op == OPERATION.DISABLE || op == OPERATION.ENABLE )) {
        throw new Exception("Please specify at least 1 path to "+params.get(1));
      }
      paths.addAll(params.subList(2, params.size()));
    }
    
    String results[] = sendRequest(token, site, op, paths);
    if(results != null) {
      System.out.println(results.length+ " api endpoints have been disabled:");
      for(String result : results) {
        System.out.println("  /api"+result);
      }
    } else {
      System.out.println("Message sent to " + op.name().toLowerCase() + " " + paths);
    }
  }
  
  /**
   * Sends acl request to cadmium.
   * 
   * @param site
   * @param op
   * @param paths
   * @return
   * @throws Exception
   */
  public static String[] sendRequest(String token, String site, OPERATION op, List<String> paths) throws Exception {
    String endpoint = null;
    HttpUriRequest message = null;
    HttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
    if(op == OPERATION.DISABLE || op == OPERATION.ENABLE) {
      if(op == OPERATION.DISABLE) {
        endpoint = DISABLE_ENDPOINT;
      } else {
        endpoint = ENABLE_ENDPOINT;
      }
      message = new HttpPost(site + endpoint);
      ((HttpPost)message).setEntity(new StringEntity(new Gson().toJson(paths), "UTF-8"));
      ((HttpPost)message).addHeader("Content-Type", MediaType.APPLICATION_JSON);
    } else {
      endpoint = LIST_ENDPOINT;
      message = new HttpGet(site + endpoint);
    }
    addAuthHeader(token, message);
    
    HttpResponse resp = client.execute(message);
    if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      String results = EntityUtils.toString(resp.getEntity());
      return new Gson().fromJson(results, String[].class);
    }
    return null;
  }

}
