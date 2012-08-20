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
package com.meltmedia.cadmium.cli;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.git.GitService;

/**
 * Initializes a Cadmium Content Project.
 * 
 * @deprecated
 * 
 * @author Chris Haley
 * @author Brian Barr
 * @author John McEntire
 *
 */
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
