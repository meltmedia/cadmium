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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.api.UpdateRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.meltmedia.cadmium.core.util.WarUtils.updateWar;

/**
 * Initializes a Cadmium site war for deployment without Cadmium Deployer war.
 * 
 * @author John McEntire
 * @author Brian Barr
 * @author Christian Trimble
 * @author Chris Haley
 *
 */
@Parameters(commandDescription="Initializes a new sites war from an existing war.", separators="=")
public class InitializeWarCommand implements CliCommand {
	
	@Parameter(names="--existingWar", description="Path to an existing cadmium war.", required=false)
	private String war;

	@Parameter(names={"--repo", "-r"}, description="Uri to remote github repo.", required=false)
	private String repoUri;  

	@Parameter(names={"--branch", "-b","--tag", "-t"}, description="Initial branch to serve content from.", required=false)
	private String branch;

  @Parameter(names={"--configuration-repo", "-R"}, description="Uri to remote github repo for configuration (only specify if different from --repo).", required=false)
  private String configRepoUri = null;  

  @Parameter(names={"--configuration-branch", "-C"}, description="Initial branch to pull configuration from.", required=false)
  private String configBranch;

	@Parameter(names="--domain", description="Sets the domain name that this war will bind to.", required=false)
	private String domain = "localhost";

  @Parameter(names="--context", description="Sets the context root that this war will bind to.", required=false)
  private String context = "/";
  
  @Parameter(names="--secure", description="Creates a Secure war.", required=false)
  private boolean secure = false;
  
  @Parameter(names="--secureContentRoot", description="Set content root.", required=false)
  private String secureContentRoot = System.getProperty("com.meltmedia.cadmium.contentRoot");

	@Parameter(description="\"new war name\"", required=true, arity=1)
	private List<String> newWarNames;
	

	public void execute() throws Exception {
		if(war == null || FileSystemManager.canRead(war)) {
			if (secure && secureContentRoot != null) {
				System.setProperty("com.meltmedia.cadmium.contentRoot", secureContentRoot);
			}
			
			if(newWarNames != null && newWarNames.size() != 0) {
			  String warName = newWarNames.get(0);
			  if(!warName.toLowerCase().trim().endsWith(".war")) {
			    warName = warName + ".war";
			    newWarNames.clear();
			    newWarNames.add(warName);
			  }
			}
			boolean validRequest = true;
	    if( StringUtils.isNotBlank(branch) && !StringUtils.startsWithIgnoreCase(branch, UpdateRequest.CONTENT_BRANCH_PREFIX + "-" )) {
	      validRequest = false;
	      System.err.println("Content branch must start with \""+UpdateRequest.CONTENT_BRANCH_PREFIX+"-\"");
	    }
	    if( StringUtils.isNotBlank(configBranch) &&  !StringUtils.startsWithIgnoreCase(configBranch, UpdateRequest.CONFIG_BRANCH_PREFIX + "-" )) {
	      validRequest = false;
	      System.err.println("Configuration branch must start with \""+UpdateRequest.CONFIG_BRANCH_PREFIX+"-\"");
	    }
	    if(!validRequest) {
	      System.exit(1);
	    }
	    updateWar("cadmium-war.war", war, newWarNames, repoUri, branch, configRepoUri, configBranch, domain, context, secure, null);
		} else {
			System.err.println("ERROR: \""+war+"\" does not exist or cannot be read.");
			System.exit(1);
		}
	}

	public String getRepoUri() {
		return repoUri;
	}

	public void setRepoUri(String repoUri) {
		this.repoUri = repoUri;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public List<String> getNewWarNames() {
		return newWarNames;
	}

	public void setNewWarNames(List<String> newWarNames) {
		this.newWarNames = newWarNames;
	}

  @Override
  public String getCommandName() {
    return "init-war";
  }
}
