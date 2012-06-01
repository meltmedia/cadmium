package com.meltmedia.cadmium.servlets.jersey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MessageSender;

public class StatusService {

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
	@Named("cadmium.properties")
	protected Properties cadmiumProperties;
	
	@Inject
	@Named("contentDir")
	protected String initialContentDir;

	// repo url, source [repo, branch, revision], environment, maintenance page state [on | off]
	
	@GET
	@Path("/status")
	@Produces("application/json")
	public String status() {
		
		Map<String, Object> returnObj = new LinkedHashMap<String, Object>();
		
		// Get current state
		UpdateState state = lifecycleService.getCurrentState();		
		
		// Get content directory
		String contentDir = this.initialContentDir;
		if(configProperties.containsKey("com.meltmedia.cadmium.lastUpdated")) {
			
			contentDir = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");			
		}
		
		
		// Get cadmium project info (branch, repo and revision)
		String rev = configProperties.getProperty("git.ref.sha");
		String branch = configProperties.getProperty("branch");
		String repo = cadmiumProperties.getProperty("com.meltmedia.cadmium.git.uri");
		
		// Get source project info (branch, repo and revision)
		String sourceRev = "";
		String sourceBranch = "";
		String sourceRepo = "";
		
		
		
		// Get cluster members' status
		List<ChannelMember> members = lifecycleService.getPeirStates();
		
		if(members != null) {
			
			List<Map<String, Object>> peers = new ArrayList<Map<String, Object>>();
			
			for(ChannelMember member : members) {
				
				Map<String, Object> peer = new LinkedHashMap<String, Object>();
				peer.put("address", member.getAddress().toString());
				peer.put("coordinator", member.isCoordinator());
				peer.put("state", member.getState().name());
				peer.put("me", member.isMine());
				peers.add(peer);
			}
			
			returnObj.put("members", peers);
		}
		
		// Get environment status TODO: setup the system property in jboss on servers
		String environment = "";
		String environFromConfig = System.getProperty("environment");
		
		if(environFromConfig != null && environFromConfig.trim().length() > 0) {			
			
			returnObj.put("environment", environment);
			
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
		returnObj.put("currentState", state.name());
		returnObj.put("branch", branch);
		returnObj.put("revision", rev);		
		returnObj.put("repo", repo);
		returnObj.put("maintPage", maintStatus);
		
		return new Gson().toJson(returnObj);
	}

}
