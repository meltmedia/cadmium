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
package com.meltmedia.cadmium.deployer;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.api.UndeployRequest;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@CadmiumSystemEndpoint
@Path("/undeploy")
public class UndeployService extends AuthorizationService {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected MessageSender sender;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public String undeploy(UndeployRequest req, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    String domain = req.getDomain();
    String contextRoot = req.getContextRoot();
    if(StringUtils.isEmptyOrNull(domain) && StringUtils.isEmptyOrNull(contextRoot)) {
      return "Invalid request";
    }
    
    if(StringUtils.isEmptyOrNull(contextRoot)) {
      contextRoot = "";
    }
    
    if(StringUtils.isEmptyOrNull(domain) || domain.equals("localhost")) {
      domain = "";
    }
    logger.debug("Sending undeploy message with domain [{}] context [{}]", domain, contextRoot);
    
    Message msg = new Message();
    msg.setCommand(UndeployCommandAction.UNDEPLOY_ACTION);
    msg.getProtocolParameters().put("domain", domain);
    msg.getProtocolParameters().put("context", contextRoot);

    sender.sendMessage(msg, null);
    return "ok";
  }
}
