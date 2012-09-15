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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jgit.util.StringUtils;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.api.DeployRequest;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.deployer.DeploymentTracker.DeploymentStatus;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@CadmiumSystemEndpoint
@Path("/deploy")
public class DeployerService extends AuthorizationService {

  private String version = "${project.version}";

  @Inject
  protected MessageSender sender;

  @Inject
  protected DeploymentTracker tracker;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces("text/plain")
  public Response deploy(DeployRequest req,
      @HeaderParam("Authorization") @DefaultValue("no token") String auth,
      @Context ServletContext context) throws Exception {
    if (!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    String branch = req.getBranch();
    String repo = req.getRepo();
    String domain = req.getDomain();
    String contextRoot = req.getContextRoot();
    String artifact = req.getArtifact();

    if (StringUtils.isEmptyOrNull(branch) || StringUtils.isEmptyOrNull(repo)
        || StringUtils.isEmptyOrNull(domain)) {
      return Response.serverError().entity("Invalid request").build();
    }

    if (StringUtils.isEmptyOrNull(contextRoot)) {
      contextRoot = "/";
    }

    if (StringUtils.isEmptyOrNull(artifact)) {
      artifact = "com.meltmedia.cadmium:cadmium-war:war:" + version;
    }

    Message msg = new Message();
    msg.setCommand(DeployCommandAction.DEPLOY_ACTION);
    msg.getProtocolParameters().put("branch", branch);
    msg.getProtocolParameters().put("repo", repo);
    msg.getProtocolParameters().put("domain", domain);
    msg.getProtocolParameters().put("context", contextRoot);
    msg.getProtocolParameters().put("secure",
        Boolean.toString(!req.isDisableSecurity()));
    msg.getProtocolParameters().put("artifact", artifact);

    sender.sendMessage(msg, null);
    return Response.created(
        new URI("/" + domain
            + (contextRoot.startsWith("/") ? contextRoot : "/" + contextRoot)))
        .build();
  }

  @GET
  @Path("/{domain}/{contextRoot}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deployStatus(@PathParam("domain") String domain,
      @PathParam("contextRoot") @DefaultValue("/") String contextRoot)
      throws URISyntaxException {
    if (StringUtils.isEmptyOrNull(contextRoot)) {
      contextRoot = "/";
    }
    try {
      DeploymentStatus status = tracker.isDeploymentComplete(domain,
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
    } catch (CadmiumDeploymentException e) {
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      return Response
          .serverError()
          .location(
              new URI("../undeploy/"
                  + domain
                  + (contextRoot.startsWith("/") ? contextRoot : "/"
                      + contextRoot))).entity(writer.toString()).build();
    }
  }

  @GET
  @Path("/{domain}")
  public Response deployStatus(@PathParam("domain") String domain)
      throws URISyntaxException {
    return deployStatus(domain, "/");
  }
}
