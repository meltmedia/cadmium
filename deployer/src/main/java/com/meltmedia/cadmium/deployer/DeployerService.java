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
	  String branch = req.getBranch();
	  String repo = req.getRepo();
    String configBranch = req.getConfigBranch();
    String configRepo = req.getConfigRepo();
	  String domain = req.getDomain();
	  String contextRoot = req.getContextRoot();
	  String artifact = req.getArtifact();
	  
	  if( StringUtils.isEmptyOrNull(branch) || StringUtils.isEmptyOrNull(configBranch) || StringUtils.isEmptyOrNull(repo) || StringUtils.isEmptyOrNull(domain) ) {
	    Response.serverError();
	    return "error";
	  }
	  
	  if( StringUtils.isEmptyOrNull(contextRoot) ) {
	    contextRoot = "/";
	  }
	  
	  if( StringUtils.isEmptyOrNull(artifact) ) {
	    artifact = "com.meltmedia.cadmium:cadmium-war:war:" + version;
	  }
		
    Message msg = new Message();
    msg.setCommand(DeployCommandAction.DEPLOY_ACTION);
    msg.getProtocolParameters().put("branch", branch);
    msg.getProtocolParameters().put("repo", repo);
    msg.getProtocolParameters().put("configBranch", configBranch);
    msg.getProtocolParameters().put("configRepo", StringUtils.isEmptyOrNull(configRepo) ? repo : configRepo);
    msg.getProtocolParameters().put("domain", domain);
    msg.getProtocolParameters().put("context", contextRoot);
    msg.getProtocolParameters().put("secure", Boolean.toString(!req.isDisableSecurity()));
    msg.getProtocolParameters().put("artifact", artifact);

    sender.sendMessage(msg, null);
    return "ok";
	}
}

