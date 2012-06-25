package com.meltmedia.cadmium.deployer;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@Path("/deploy")
public class DeployerService extends AuthorizationService {
	private final Logger logger = LoggerFactory.getLogger(getClass()); 

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/plain")
	public String deploy(@FormParam("branch") String branch, @FormParam("repo") String repo, @FormParam("domain") String domain, @HeaderParam("Authorization") @DefaultValue("no token") String auth, @Context ServletContext context) throws Exception {
	  if(!this.isAuth(auth)) {
	    throw new Exception("Unauthorized!");
    }
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("branch", branch);
		parameters.put("repo", repo);
		parameters.put("domain", domain);
		
		String message = new Gson().toJson(parameters);
		logger.debug("Sending [{}] over jgroups", message);
		
		JChannel channel = (JChannel)context.getAttribute(JGroupsMessagingListener.JGROUPS_CHANNEL);
		channel.send(new Message(null, null, message));
		
		return "ok";
	} 
}

