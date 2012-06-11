package com.meltmedia.cadmium.servlets.jersey;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Path("/maintenance")
public class MaintenanceService extends AuthorizationService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
  protected MessageSender sender;
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/plain")
	public String post(@FormParam("state") String state,@FormParam("comment") String comment, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
	  if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    Message msg = new Message();
    log.info("state: " + state);
    log.info("comment: " + comment);
    msg.setCommand(ProtocolMessage.MAINTENANCE);
    if(state != null && (state.trim().equalsIgnoreCase("on") || state.trim().equalsIgnoreCase("off"))) {
    	msg.getProtocolParameters().put("state", state);
    	if(comment != null && comment.trim().length() > 0) {
      	msg.getProtocolParameters().put("comment", comment);
      }
    	msg.getProtocolParameters().put("openId", openId);
    	sender.sendMessage(msg, null);
    	return "ok";
    }
    return "invalid request";
	}
}
