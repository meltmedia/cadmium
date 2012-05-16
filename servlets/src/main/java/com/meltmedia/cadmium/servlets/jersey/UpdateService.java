package com.meltmedia.cadmium.servlets.jersey;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Path("/update")
public class UpdateService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  protected MessageSender sender;
  
  @Inject
  protected SiteDownService sd;
  
  @GET
  @Produces("text/plain")
  public String update(@QueryParam("branch") String branch, @QueryParam("sha") String sha) throws Exception {
    if(sender != null) {
      log.debug("Sending update message");
      Message msg = new Message();
      msg.setCommand(ProtocolMessage.UPDATE);
      if(branch != null && branch.trim().length() > 0) {
        msg.getProtocolParameters().put("branch", branch);
      }
      if(sha != null && sha.trim().length() > 0) {
        msg.getProtocolParameters().put("sha", sha);
      }
      sender.sendMessage(msg, null);
    } else {
      log.error("Channel is not wired");
    }
    return "ok";
  }
  
  @GET
  @Path("/start")
  @Produces("text/plain")
  public String start() {
    sd.start();
    return "ok";
  }
  
  @GET
  @Path("/stop")
  @Produces("text/plain")
  public String stop() {
    sd.stop();
    return "ok";
  }
}
