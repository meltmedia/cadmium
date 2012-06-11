package com.meltmedia.cadmium.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.cli.InitializeWarCommand;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@Path("/deploy")
public class DeployerService extends AuthorizationService {
	private final Logger log = LoggerFactory.getLogger(getClass()); 

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/plain")
	public String deploy(@FormParam("branch") String branch, @FormParam("repo") String repo, @FormParam("domain") String domain, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
	  if(!this.isAuth(auth)) {
	    throw new Exception("Unauthorized!");
    }
		InitializeWarCommand initCommand = new InitializeWarCommand();
		
		initCommand.setBranch(branch);
		initCommand.setDomain(domain);
		initCommand.setRepoUri(repo);
		
		log.info("Beginning war creation. branch: {}, repo {}, domain {}", new String[]{branch, repo, domain});
		
		
		//setup war name and inset into list in initCommand
		List<String> newWarNames = new ArrayList<String>();
		String tmpFileName = domain.replace("\\.", "_") + ".war";		
		File tmpZip = File.createTempFile(tmpFileName, null);
		tmpZip.delete();
		newWarNames.add(tmpZip.getAbsolutePath());
		initCommand.setNewWarNames(newWarNames);
		initCommand.execute();
		
		String deployPath = System.getProperty("jboss.server.home.dir", "/opt/jboss/server/meltmedia") + "/deploy";
		
		tmpZip.renameTo(new File(deployPath, tmpFileName));
		
		return "ok";
	} 
}

