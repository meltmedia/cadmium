package com.meltmedia.cadmium.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.git.GitService;


@Parameters(commandDescription = "Initializes New Cadmium Project", separators="=")
public class InitializeProjectCommand {
	
	@Parameter(names="--site", description="Site Name", required=true)
	private String site;
	
	private GitService gitService;
	
	public void execute() throws Exception {
		
		gitService = GitService.init(site,System.getProperty("user.dir")); 
		if (gitService != null) {
			BranchCreator creator = new BranchCreator(gitService.getRepositoryDirectory());
			creator.createBranchForDevAndQa("launch");
			creator.createBranchForGene("launch");
			System.out.println("Note: This Git Repo is only created locally, still needs pushed to GitHub!");
		}
		
	}

}
