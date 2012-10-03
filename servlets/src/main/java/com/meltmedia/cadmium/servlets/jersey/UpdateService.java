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

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.jgit.util.StringUtils;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.api.BasicApiResponse;
import com.meltmedia.cadmium.core.api.UpdateRequest;
import com.meltmedia.cadmium.core.commands.ContentUpdateRequest;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@CadmiumSystemEndpoint
@Path("/update")
public class UpdateService extends AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  protected MessageSender sender;
  
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public BasicApiResponse update(UpdateRequest req, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    return sendJgroupsMessage(req, auth, ProtocolMessage.UPDATE);
  }
  
  @POST
  @Path("/config")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public BasicApiResponse updateConfig(UpdateRequest req, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    return sendJgroupsMessage(req, auth, ProtocolMessage.CONFIG_UPDATE);
  }

  private BasicApiResponse sendJgroupsMessage(UpdateRequest req, String auth, String cmd)
      throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    
    BasicApiResponse resp = new BasicApiResponse();
    resp.setUuid(UUID.randomUUID().toString());
    
    if( sender == null ) {
      resp.setMessage("Cadmium is not fully deployed. See logs for details.");
      log.error("Channel is not wired");
      return resp;
    }
    else if( StringUtils.isEmptyOrNull(req.getComment()) ) {
      resp.setMessage("invalid request");
      return resp;
    }
    
    // NOTE: if the headers had the openId and UUID, then we could reuse the request from the client.
    ContentUpdateRequest body = new ContentUpdateRequest();
    body.setRepo(emptyStringIfNull(req.getRepo()));
    body.setBranchName(emptyStringIfNull(req.getBranch()));
    body.setSha(emptyStringIfNull(req.getSha()));
    body.setComment(req.getComment());
    body.setOpenId(openId);
    body.setUuid(resp.getUuid());
    Message<ContentUpdateRequest> msg = new Message<ContentUpdateRequest>(cmd, body);
    sender.sendMessage(msg, null);
    resp.setMessage("ok");

    return resp;
  }
  
  private static String emptyStringIfNull( final String value ) {
    if( value == null ) return "";
    else return value;
  }

  
}
