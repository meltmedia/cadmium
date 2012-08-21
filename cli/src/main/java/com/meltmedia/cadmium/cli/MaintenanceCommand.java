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

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.meltmedia.cadmium.core.api.BasicApiResponse;
import com.meltmedia.cadmium.core.api.MaintenanceRequest;

/**
 * Controls a Cadmium sites maintenance page. 
 * 
 * @author Chris Haley
 * @author John McEntire
 * @author Brian Barr
 * @author Christian Trimble
 *
 */
@Parameters(commandDescription = "Toggles on and off maintenance page", separators="=")
public class MaintenanceCommand extends AbstractAuthorizedOnly implements CliCommand {

	@Parameter(description="<on|off> <site>", required=true)
	private List<String> paramList;

	@Parameter(names={"--message", "-m"}, description="Comment", required=false)
	private String message = "toggling maintenance page";

	private String site;
	private MaintenanceRequest.State state;
	private final String JERSEY_ENDPOINT = "/system/maintenance";

	public void execute() throws ClientProtocolException, IOException, Exception {
		
		DefaultHttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());

		if(paramList.size() == 2) {
		  try {
		    state = MaintenanceRequest.State.valueOf(paramList.get(0).toUpperCase());
		  } catch(Exception e) {
		  }
			site = getSecureBaseUrl(paramList.get(1));
			String url = site + JERSEY_ENDPOINT;
			if(state != null) {
				HttpPost post = new HttpPost(url);
		    addAuthHeader(post);
		    post.setHeader("Content-Type", MediaType.APPLICATION_JSON);
		    
		    MaintenanceRequest req = new MaintenanceRequest();
		    req.setState(state);
		    req.setComment(message);
	
				post.setEntity(new StringEntity(new Gson().toJson(req), "UTF-8"));
				HttpResponse response = client.execute(post);
	
	      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	        HttpEntity entity = response.getEntity();
	        String resp = EntityUtils.toString(entity);
	        BasicApiResponse respObj = new Gson().fromJson(resp, BasicApiResponse.class);
	        if(respObj.getMessage().equalsIgnoreCase("ok")) {
	          System.out.println("Success!");
	        } else {
	          System.out.println(respObj.getMessage());
	        }
	      } else {
	        System.out.println(response.toString());
	      }
			} else {
				System.err.println("Invalid State. Please use 'on' or 'off'.");
			}
		} else {
			System.err.println("Invalid Number of Parameters");
		}
	}

  @Override
  public String getCommandName() {
    return "maint";
  }

}
