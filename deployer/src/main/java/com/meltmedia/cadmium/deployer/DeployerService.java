package com.meltmedia.cadmium.deployer;


import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@Path("/deploy")
public class DeployerService extends AuthorizationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
  @Inject
  protected MessageSender sender;

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/plain")
	public String deploy(@FormParam("branch") String branch, @FormParam("repo") String repo, @FormParam("domain") @DefaultValue("localhost") String domain, @FormParam("context") @DefaultValue("/") String contextRoot, @HeaderParam("Authorization") @DefaultValue("no token") String auth, @Context ServletContext context) throws Exception {
	  if(!this.isAuth(auth)) {
	    throw new Exception("Unauthorized!");
    }
	  
	  if( StringUtils.isEmptyOrNull(branch) || StringUtils.isEmptyOrNull(repo) || StringUtils.isEmptyOrNull(domain) ) {
	    Response.serverError();
	    return "error";
	  }
	  
	  if( StringUtils.isEmptyOrNull(contextRoot) ) {
	    contextRoot = "/";
	  }
		
    Message msg = new Message();
    msg.setCommand(DeployCommandAction.DEPLOY_ACTION);
    msg.getProtocolParameters().put("branch", branch);
    msg.getProtocolParameters().put("repo", repo);
    msg.getProtocolParameters().put("domain", domain);
    msg.getProtocolParameters().put("context", contextRoot);

    sender.sendMessage(msg, null);
    return "ok";
	}
}

