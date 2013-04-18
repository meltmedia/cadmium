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

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.ClusterMembers;
import com.meltmedia.cadmium.core.api.DeployRequest;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@CadmiumSystemEndpoint
@Path("/deploy")
public class DeployerService extends AuthorizationService {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private String version = "LATEST";
	
  @Inject
  protected MessageSender sender;

  @Inject
  protected DeployResponseCommandAction response;

  @Inject
  protected DeploymentCheckResponseCommandAction checkResponse;

  @Inject
  @ClusterMembers
  protected List<ChannelMember> members;

  @Inject
  protected MembershipTracker membershipTracker;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("text/plain")
	public String deploy(DeployRequest req, @HeaderParam("Authorization") @DefaultValue("no token") String auth, @Context ServletContext context) throws Exception {
	  if(!this.isAuth(auth)) {
	    throw new Exception("Unauthorized!");
    }

	  String contextRoot = req.getContextRoot();
	  String artifact = req.getArtifact();
	  
	  if( StringUtils.isEmptyOrNull(req.getRepo()) ||
	      StringUtils.isEmptyOrNull(req.getDomain()) ) {
	    return "Error: missing repo and/or domain.";
	  }
	  
	  if( StringUtils.isEmptyOrNull(contextRoot) ) {
	    contextRoot = "/";
	  }
	  
	  if( StringUtils.isEmptyOrNull(artifact) ) {
	    artifact = "com.meltmedia.cadmium:cadmium-war:war:" + version;
	  }
	  
	  if( StringUtils.isEmptyOrNull(req.getConfigRepo() ) ) {
	    req.setConfigRepo(req.getRepo());
	  }
    
    if( StringUtils.isEmptyOrNull(req.getConfigBranch() ) ) {
      req.setConfigBranch("master");
    }
    
    if( StringUtils.isEmptyOrNull(req.getBranch() ) ) {
      req.setBranch("master");
    }


    ChannelMember coordinator = membershipTracker.getCoordinator();
	  com.meltmedia.cadmium.deployer.DeployRequest mRequest = new com.meltmedia.cadmium.deployer.DeployRequest();
	  mRequest.setBranch(req.getBranch());
	  mRequest.setRepo(req.getRepo());
	  mRequest.setConfigBranch(req.getConfigBranch());
	  mRequest.setConfigRepo(req.getConfigRepo());
	  mRequest.setDomain(req.getDomain());
	  mRequest.setContext(contextRoot);
	  mRequest.setSecure(!req.isDisableSecurity());
	  mRequest.setArtifact(artifact);
    Message<com.meltmedia.cadmium.deployer.DeployRequest> msg = new Message<com.meltmedia.cadmium.deployer.DeployRequest>(DeployCommandAction.DEPLOY_ACTION, mRequest);
    response.reset(coordinator);
    sender.sendMessage(msg, coordinator);

    int timeout = 4800;
    while (timeout-- > 0) {
      Thread.sleep(500l);
      Message<DeployResponse> returnMsg = response.getResponse(coordinator);
      if (returnMsg != null) {
        if(returnMsg.getBody().getError() != null) {
          throw new Exception(returnMsg.getBody().getError());
        }
        return returnMsg.getBody().getWarName();
      }
    }

    return "ok";
	}

  @GET
  @Path("{warName}")
  public Response deploymentState(@PathParam("warName") String warName) throws Exception {
    DeploymentCheckRequest request = new DeploymentCheckRequest();
    request.setWarName(warName);
    Message<DeploymentCheckRequest> msg = new Message<DeploymentCheckRequest>(DeploymentCheckCommandAction.COMMAND_ACTION, request);
    checkResponse.resetAll();
    sender.sendMessage(msg, null);
    Map<ChannelMember, DeploymentCheckResponse> responses = new HashMap<ChannelMember, DeploymentCheckResponse>();
    int timeout = 240;
    while(timeout-- > 0) {
      Thread.sleep(500l);
      boolean foundAll = true;
      for(ChannelMember mem: members) {
        Message<DeploymentCheckResponse> memResponse = checkResponse.getResponse(mem);
        if(memResponse != null) {
          responses.put(mem, memResponse.getBody());
        } else {
          foundAll = false;
        }
      }
      if(foundAll) {
        break;
      }
    }
    if(responses.size() == members.size()) {
      Set<ChannelMember> membersInError = new HashSet<ChannelMember>();
      boolean combinedState = true;
      boolean anyStarted = false;
      for(ChannelMember mem : responses.keySet()) {
        DeploymentCheckResponse response = responses.get(mem);
        if(response.getError() != null) {
          combinedState = false;
          membersInError.add(mem);
        } else {
          combinedState = combinedState && response.isFinished();
        }
        if(response.isStarted()) {
          anyStarted = true;
        }
      }
      Map<String, Object> responseObj = new LinkedHashMap<String, Object>();
      responseObj.put("finished", combinedState);
      responseObj.put("started", anyStarted);
      if(membersInError.size() > 0) {
        Set<String> inError = new TreeSet<String>();
        responseObj.put("errors", inError);
        for(ChannelMember mem : membersInError) {
          inError.add(mem.getAddress().toString() + (mem.isCoordinator()? ":coord" : ""));
        }
      }
      return Response.ok(new Gson().toJson(responseObj)).build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }
}

