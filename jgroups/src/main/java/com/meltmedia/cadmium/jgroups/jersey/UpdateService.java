package com.meltmedia.cadmium.jgroups.jersey;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.jgroups.receivers.UpdateChannelReceiver;

@Path("/update")
public class UpdateService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  protected JChannel channel;
  
  @POST
  public void update() throws Exception {
    if(channel != null) {
      log.debug("Sending update message");
      channel.send(new Message(null, null, UpdateChannelReceiver.ProtocolMessage.UPDATE.name()));
    } else {
      log.error("Channel is not wired");
    }
  }
}
