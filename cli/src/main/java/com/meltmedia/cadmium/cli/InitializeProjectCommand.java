package com.meltmedia.cadmium.cli;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.git.GitService;


@Parameters(commandDescription = "Initializes New Cadmium Project", separators="=")
public class InitializeProjectCommand implements CliCommand {
	
	@Parameter(description="<site>", required=true)
	private List<String> site;
	
	private GitService gitService;
	
	public void execute() throws Exception {
		
		gitService = GitService.init(site.get(0),System.getProperty("user.dir")); 
		if (gitService != null) {
			BranchCreator creator = new BranchCreator(gitService.getRepositoryDirectory());
			creator.createBranchForDevAndQa("launch");
			creator.createBranchForGene("launch");
			System.out.println("Note: This Git Repo is only created locally, still needs to be pushed to GitHub!");
		}
		
	}

  @Override
  public String getCommandName() {
    return "init-project";
  }

}
