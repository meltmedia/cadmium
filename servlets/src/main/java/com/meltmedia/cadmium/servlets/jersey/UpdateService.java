package com.meltmedia.cadmium.servlets.jersey;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Path("/update")
public class UpdateService extends AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  protected MessageSender sender;
  
  @Inject
  protected SiteDownService sd;
  
  @Inject
  protected LifecycleService lifecycleService;
  
  @Inject
  @Named("config.properties")
  protected Properties configProperties;
  
  @Inject
  @Named("contentDir")
  protected String initialContentDir;
  
  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces("text/plain")
  public String update(@FormParam("branch") String branch, @FormParam("sha") String sha, @FormParam("comment") String comment, @HeaderParam("Authorization") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
   
    if(sender != null) {
      if(comment != null && comment.trim().length() > 0) {
        log.debug("Sending update message");
        Message msg = new Message();
        msg.setCommand(ProtocolMessage.UPDATE);
        if(branch != null && branch.trim().length() > 0) {
          msg.getProtocolParameters().put("branch", branch);
        }
        if(sha != null && sha.trim().length() > 0) {
          msg.getProtocolParameters().put("sha", sha);
        }
        msg.getProtocolParameters().put("comment", comment);
        msg.getProtocolParameters().put("openId", openId);
        sender.sendMessage(msg, null);
      } else {
        return "invalid request\n";
      }
    } else {
      log.error("Channel is not wired");
    }
    return "ok";
  } 
  
}
