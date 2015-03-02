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

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
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
  private static final String ENDPOINT = "/system/disabled/";
  private enum OPERATION { DISABLE, ENABLE, LIST; }
  
  @Parameter(description = "<site> (list|disable|enable) [endpoint]", required = true)
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
    
    String site = getSecureBaseUrl(params.get(0).replaceFirst("/$", ""));
    OPERATION op = OPERATION.LIST;
    String path = null;
    if(params.size() >= 2) {
      op = OPERATION.valueOf(params.get(1).toUpperCase());
      if(params.size() <= 2 && ( op == OPERATION.DISABLE || op == OPERATION.ENABLE )) {
        throw new Exception("Please specify a path to "+params.get(1));
      } else if (op == OPERATION.DISABLE || op == OPERATION.ENABLE) {
        path = params.get(2).replaceAll("(^/)|(/$)", "");
      }
    }
    
    String results[] = sendRequest(token, site, op, path);
    if(results != null) {
      System.out.println(results.length+ " api endpoints have been disabled:");
      for(String result : results) {
        System.out.println("  /api/"+result);
      }
    } else {
      System.out.println("Message sent to " + op.name().toLowerCase() + " " + path);
    }
  }
  
  /**
   * Sends acl request to cadmium.
   * 
   * @param site
   * @param op
   * @param path
   * @return
   * @throws Exception
   */
  public static String[] sendRequest(String token, String site, OPERATION op, String path) throws Exception {
    HttpClient client = httpClient();
    HttpUriRequest message = null;
    if(op == OPERATION.DISABLE) {
      message = new HttpPut(site + ENDPOINT + path);
    } else if(op == OPERATION.ENABLE){
      message = new HttpDelete(site + ENDPOINT + path);
    } else {
      message = new HttpGet(site + ENDPOINT);
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
