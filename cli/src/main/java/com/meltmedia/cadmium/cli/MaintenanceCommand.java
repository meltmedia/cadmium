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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Toggles on and off maintenance page", separators="=")
public class MaintenanceCommand extends AbstractAuthorizedOnly implements CliCommand {

	@Parameter(description="<on|off> <site>", required=true)
	private List<String> paramList;

	@Parameter(names={"--message", "-m"}, description="Comment", required=false)
	private String message = "toggling maintenance page";

	private String site;
	private String state;
	private final String JERSEY_ENDPOINT = "/system/maintenance";

	public void execute() throws ClientProtocolException, IOException {
		
		DefaultHttpClient client = new DefaultHttpClient();

		if(paramList.size() == 2) {
			state = paramList.get(0);
			site = paramList.get(1);
			String url = site + JERSEY_ENDPOINT;
			if(state.trim().equalsIgnoreCase("on") || state.trim().equalsIgnoreCase("off")) {
				HttpPost post = new HttpPost(url);
		    addAuthHeader(post);
		    
				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair("state", state.trim()));
				nvps.add(new BasicNameValuePair("comment", message.trim()));
	
				post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
				HttpResponse response = client.execute(post);
	
	      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	        HttpEntity entity = response.getEntity();
	        String resp = EntityUtils.toString(entity);
	        if(resp.equalsIgnoreCase("ok")) {
	          System.out.println("Success!");
	        } else {
	          System.out.println(resp);
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
