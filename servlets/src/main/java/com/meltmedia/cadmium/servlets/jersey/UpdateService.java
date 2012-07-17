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

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.api.BasicApiResponse;
import com.meltmedia.cadmium.core.api.UpdateRequest;
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
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public BasicApiResponse update(UpdateRequest req, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    String branch = "";
    String sha = "";
    String comment = req.getComment();
    if(req.getBranch() != null) {
      branch = req.getBranch();
    }
    if(req.getSha() != null) {
      sha = req.getSha();
    }
    BasicApiResponse resp = new BasicApiResponse();
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
        resp.setMessage("ok");
      } else {
        resp.setMessage("invalid request");
      }
    } else {
      resp.setMessage("Cadmium is not fully deployed. See logs for details.");
      log.error("Channel is not wired");
    }
    return resp;
  } 
  
}
