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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.api.BasicApiResponse;
import com.meltmedia.cadmium.core.api.MaintenanceRequest;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Path("/maintenance")
public class MaintenanceService extends AuthorizationService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
  protected MessageSender sender;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BasicApiResponse post(MaintenanceRequest request, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
	  if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
	  String comment = request.getComment();
    Message msg = new Message();
    log.info("state: " + request.getState());
    log.info("comment: " + comment);
    msg.setCommand(ProtocolMessage.MAINTENANCE);
    BasicApiResponse cmdResponse = new BasicApiResponse();
    if(request.getState() != null) {
    	msg.getProtocolParameters().put("state", request.getState().name());
    	if(comment != null && comment.trim().length() > 0) {
      	msg.getProtocolParameters().put("comment", comment);
      }
    	msg.getProtocolParameters().put("openId", openId);
    	sender.sendMessage(msg, null);
    	
    	cmdResponse.setMessage("ok");
    } else {
      cmdResponse.setMessage("invalid request");
    }
    return cmdResponse;
	}
}
