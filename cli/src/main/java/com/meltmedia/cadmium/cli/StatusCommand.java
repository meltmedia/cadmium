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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.status.Status;
import com.meltmedia.cadmium.status.StatusMember;


@Parameters(commandDescription = "Displays status info for a site")
public class StatusCommand extends AbstractAuthorizedOnly implements CliCommand {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Parameter(description="<site>", required=true)
	private List<String> site;	

	public static final String JERSEY_ENDPOINT = "/system/status";

	public void execute() throws ClientProtocolException, IOException {

		DefaultHttpClient client = new DefaultHttpClient();
		
		String siteUrl = site.get(0);		
		String url = siteUrl + JERSEY_ENDPOINT;	
		
		log.debug("site + JERSEY_ENDPOINT = {}", url);
		
		HttpGet get = new HttpGet(url);
    addAuthHeader(get);
    
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		
		log.debug("entity content type: {}", entity.getContentType().getValue());
		if(entity.getContentType().getValue().equals("application/json")) {				
						
            String responseContent = EntityUtils.toString(entity);  
            log.debug("responseContent: {}" + responseContent);
            Status statusObj = new Gson().fromJson(responseContent, new TypeToken<Status>() {}.getType());    
            List<StatusMember> members = statusObj.getMembers();
            
            log.debug(statusObj.toString());              
           
            System.out.println();
            System.out.println("Current status for [" + siteUrl +"]"); 
            System.out.println("-----------------------------------------------------");
            System.out.println(
            		"Environment      => [" + statusObj.getEnvironment() + "]\n" +
            		"Repo URL         => [" + statusObj.getRepo() + "]\n" +
            		"Branch           => [" + statusObj.getBranch() + "]\n" +
            		"Revision         => [" + statusObj.getRevision() + "]\n" +
            		"Content Source   => [\n" + statusObj.getSource() + "]\n" +
            		"Maint Page State => [" + statusObj.getMaintPageState() +"]\n");  
            
            System.out.println();
            System.out.println("Member States:\n");
            System.out.println("-----------------------------------------------------");
            for(StatusMember member : members) {
            	System.out.println(
            			"   Address         : [" + member.getAddress() + "]\n" +
            			"   Is Coordinator? : [" + member.isCoordinator() + "]\n" +
            			"   State           : [" + member.getState() + "]\n" +
            			"   Is Me?          : [" + member.isMine() + "]\n"  	
            			            	
            	);
            }
            
		}		
			
	}

  @Override
  public String getCommandName() {
    return "status";
  }

}
