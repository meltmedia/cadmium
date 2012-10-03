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
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.util.StringUtils;

import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.api.DeployRequest;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@CadmiumSystemEndpoint
@Path("/deploy")
public class DeployerService extends AuthorizationService {
  
  private String version = "${project.version}";
	
  @Inject
  protected MessageSender sender;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("text/plain")
	public String deploy(DeployRequest req, @HeaderParam("Authorization") @DefaultValue("no token") String auth, @Context ServletContext context) throws Exception {
	  if(!this.isAuth(auth)) {
	    throw new Exception("Unauthorized!");
    }

	  String contextRoot = req.getContextRoot();
	  String artifact = req.getArtifact();
	  
	  if( StringUtils.isEmptyOrNull(req.getBranch()) ||
	      StringUtils.isEmptyOrNull(req.getConfigBranch()) ||
	      StringUtils.isEmptyOrNull(req.getRepo()) ||
	      StringUtils.isEmptyOrNull(req.getDomain()) ) {
	    Response.serverError();
	    return "error";
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

    sender.sendMessage(msg, null);
    return "ok";
	}
}

