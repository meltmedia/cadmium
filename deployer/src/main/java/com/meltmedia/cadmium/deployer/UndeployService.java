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

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.api.UndeployRequest;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.deployer.DeploymentTracker.UndeploymentStatus;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@CadmiumSystemEndpoint
@Path("/undeploy")
public class UndeployService extends AuthorizationService {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected MessageSender sender;

  @Inject
  protected DeploymentTracker tracker;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response undeploy(UndeployRequest req, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    String domain = req.getDomain();
    String contextRoot = req.getContextRoot();
    if(StringUtils.isEmptyOrNull(domain) && StringUtils.isEmptyOrNull(contextRoot)) {
      return Response.serverError().entity("Invalid request").build();
    }
    
    if(StringUtils.isEmptyOrNull(contextRoot)) {
      contextRoot = "";
    }
    
    if(StringUtils.isEmptyOrNull(domain) || domain.equals("localhost")) {
      domain = "";
    }
    sendUndeployMessage(sender, domain, contextRoot, logger);
    return Response.created(
        new URI("/" + domain
            + (contextRoot.startsWith("/") ? contextRoot : "/" + contextRoot)))
        .build();
  }

  @GET
  @Path("/{domain}/{contextRoot}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response undeployStatus(@PathParam("domain") String domain,
      @PathParam("contextRoot") @DefaultValue("/") String contextRoot)
      throws URISyntaxException {
    if (StringUtils.isEmptyOrNull(contextRoot)) {
      contextRoot = "/";
    }
    UndeploymentStatus status = tracker.getUndeployment(domain,
        contextRoot);
    if (status != null) {
      if (status.isFinished()) {
        return Response.ok().entity(new Gson().toJson(status)).build();
      } else {
        return Response.status(Status.ACCEPTED)
            .entity(new Gson().toJson(status)).build();
      }
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("/{domain}")
  public Response undeployStatus(@PathParam("domain") String domain)
      throws URISyntaxException {
    return undeployStatus(domain, "/");
  }

  public static void sendUndeployMessage(MessageSender sender, String domain, String contextRoot, Logger log)
      throws Exception {
    log.debug("Sending undeploy message with domain [{}] context [{}]", domain, contextRoot);
    
    Message msg = new Message();
    msg.setCommand(UndeployCommandAction.UNDEPLOY_ACTION);
    msg.getProtocolParameters().put("domain", domain);
    msg.getProtocolParameters().put("context", contextRoot);

    sender.sendMessage(msg, null);
  }
}
