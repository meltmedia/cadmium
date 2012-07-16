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
package com.meltmedia.cadmium.servlets.jersey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MessageSender;

@Path("/status")
public class StatusService extends AuthorizationService {

	//constants
	public final String ENVIRON_DEV = "dev";
	public final String ENVIRON_QA = "qa";
	public final String ENVIRON_GENE = "gene";
	public final String ENVIRON_PROD = "prod";
	public final String MAINT_ON = "on";
	public final String MAINT_OFF = "off";
	
	@Inject
	protected MessageSender sender;
	
	@Inject
	protected SiteDownService maintService;
	
	@Inject
	protected LifecycleService lifecycleService;

	@Inject
	@Named("config.properties")
	protected Properties configProperties;
	
	@Inject
	@Named("com.meltmedia.cadmium.git.uri")
	protected String repoUri;
	
	@Inject
	@Named("contentDir")
	protected String initialContentDir;

	@GET
	@Path("/Ping")
	@Produces("text/plain")
	public String ping() {
	  return "Ok";
	}
	
	@GET	
	@Produces("application/json")
	public String status(@HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
	  if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    
		Map<String, Object> returnObj = new LinkedHashMap<String, Object>();
		
		// Get content directory
		String contentDir = this.initialContentDir;
		if(configProperties.containsKey("com.meltmedia.cadmium.lastUpdated")) {
			
			contentDir = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");			
		}
		
		
		// Get cadmium project info (branch, repo and revision)
		String rev = configProperties.getProperty("git.ref.sha");
		String branch = configProperties.getProperty("branch");
		String repo = repoUri;
		
		// Get source project info (branch, repo and revision)
		String source = configProperties.getProperty("source", "{}");
		
		
		
		// Get cluster members' status
		List<ChannelMember> members = lifecycleService.getPeirStates();
		
		if(members != null) {
			
			List<Map<String, Object>> peers = new ArrayList<Map<String, Object>>();
			
			for(ChannelMember member : members) {
				
				Map<String, Object> peer = new LinkedHashMap<String, Object>();
				peer.put("address", member.getAddress().toString());
				peer.put("coordinator", member.isCoordinator());
				peer.put("state", member.getState().name());
				peer.put("mine", member.isMine());
				peers.add(peer);			
				
			}
			
			returnObj.put("members", peers);
		}
		
		// Get environment status 
		String environFromConfig = System.getProperty("com.meltmedia.cadmium.environment");
		
		if(environFromConfig != null && environFromConfig.trim().length() > 0) {			
			
			returnObj.put("environment", environFromConfig);
			
		}
		else {
			
			returnObj.put("environment", ENVIRON_DEV);	
		}
		
		// Get Maintanence page status (on or off)
		String maintStatus;
		if(maintService.isOn()) {
			
			maintStatus = MAINT_ON;
		}
		else {
			
			maintStatus = MAINT_OFF;
		}
		
		returnObj.put("groupName", sender.getGroupName());
		returnObj.put("contentDir", contentDir);
		returnObj.put("branch", branch);
		returnObj.put("revision", rev);		
		returnObj.put("repo", repo);
		returnObj.put("maintPageState", maintStatus);
		returnObj.put("source", source);
		
		return new Gson().toJson(returnObj);
	}

}
