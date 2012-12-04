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

import java.io.File;
import java.util.List;

import org.eclipse.jgit.util.StringUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.api.UpdateRequest;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.status.Status;

/**
 * <p>CLI Command that clones the content from a source to a target.</p>
 * <p>This simply checks the status of both source and target sites. 
 * Then, as long as they are both using the same Git repository, 
 * issues an update command on the target site with the branch and revision of the source site.</p>
 * <p>The @Parameters of this class get wired up by JCommander.</p>
 * 
 * @see <a href="http://jcommander.org/">JCommander</a>
 * 
 * @author John McEntire
 *
 */
@Parameters(commandDescription="This command will move all content from one branch to another or tag a version of a branch.", separators="=")
public class CloneCommand extends AbstractAuthorizedOnly implements CliCommand {

  /**
   * A collection of strings that will be assigned the raw parameters that are not associated with an option.
   * 
   * @see <a href="http://jcommander.org/">JCommander</a>
   */
  @Parameter(description="<source-site> <target-site>", required=true)
  private List<String> sites;
  
  /**
   * The command to log in the update history of the target site.
   * 
   * @see <a href="http://jcommander.org/">JCommander</a>
   */
  @Parameter(names={"--message", "-m"}, description="comment", required=true)
  private String comment;
  
  /**
   * 
   * @see <a href="http://jcommander.org/">JCommander</a>
   */
  @Parameter(names={"--include-config", "-c"}, description="Clone configuration", required=false)
  private boolean includeConfig = false;
  
  /**
   * Called to execute this command.
   */
  public void execute() throws Exception {
    String site1 = getSecureBaseUrl(sites.get(0));
    String site2 = getSecureBaseUrl(sites.get(1));
    if(site1.equalsIgnoreCase(site2)) {
      System.err.println("Cannot clone a site into itself.");
      System.exit(1);
    }
    
    try{
      System.out.println("Getting status of ["+site1+"]");
      Status site1Status = StatusCommand.getSiteStatus(site1, token);
      
      System.out.println("Getting status of ["+site2+"]");
      Status site2Status = StatusCommand.getSiteStatus(site2, token);
      if(site1Status != null && site2Status != null) {
        
        String repo = site1Status.getRepo();
        String revision = site1Status.getRevision();
        String branch = site1Status.getBranch();
        
        if(site2Status.getRepo().equals(repo) && site2Status.getBranch().equals(branch) && site2Status.getRevision().equals(revision)) {
          System.err.println("Source [" + site1 + "] is on the same content repo, branch, and revision as the target [" + site2 + "].");
          checkSendUpdateConfigMessage(site1, site2, site1Status, site2Status);
          System.exit(1);
        }
        
        System.out.println("Sending update message to ["+site2+"]");
        UpdateCommand.sendUpdateMessage(site2, repo, branch, revision, "Cloned from ["+site1+"]: " + comment, token);
        checkSendUpdateConfigMessage(site1, site2, site1Status, site2Status);
      } else {
        System.err.println("Failed to get status from source and/or target.");
        System.exit(1);
      }
    } catch(Exception e) {
      e.printStackTrace();
      System.err.println("Failed to clone ["+site1+"] to ["+site2+"]: "+e.getMessage());
    }
  }

  /**
   * Checks if a config update message is necessary and sends it if it is.
   * 
   * @param site1 The source site for the configuration.
   * @param site2 The target site for the configuration.
   * @param site1Status The status of the source site.
   * @param site2Status The status of the target site.
   * @throws Exception
   */
  private void checkSendUpdateConfigMessage(String site1, String site2,
      Status site1Status, Status site2Status) throws Exception {
    String repo;
    String revision;
    String branch;
    if(includeConfig) {
      repo = site1Status.getConfigRepo();
      revision = site1Status.getConfigRevision();
      branch = site1Status.getConfigBranch();
      if(!StringUtils.isEmptyOrNull(repo) && !StringUtils.isEmptyOrNull(revision) && !StringUtils.isEmptyOrNull(branch)) {
        if(!repo.equals(site2Status.getConfigRepo()) || !revision.equals(site2Status.getConfigRevision()) || ! branch.equals(site2Status.getConfigBranch())) {
          System.out.println("Sending update/config message to ["+site2+"]");
          UpdateCommand.sendUpdateMessage(site2, repo, branch, revision, "Cloned config from ["+site1+"]: " + comment, token, UpdateConfigCommand.UPDATE_CONFIG_ENDPOINT, UpdateRequest.CONFIG_BRANCH_PREFIX);
        } else {
          System.out.println("Source [" + site1 + "] is on the same configuration repo, branch, and revision as the target [" + site2 + "].");
        }
      } else {
        System.out.println("Configuration status not available from source site [" + site1 + "]");
      }
    }
  }

  /**
   * Replaces the content contained within the Git repository branch pointed to by the {@link GitService} object with the source.
   * 
   * @param source The directory to commit the content from.
   * @param service The {@link GitService} pointing to the repository and branch to commit to.
   * @param comment The commit message.
   * @return The new revision.
   * @throws Exception
   */
  public static String cloneContent(String source, GitService service, String comment) throws Exception {
    String rev = GitService.moveContentToBranch(source, service, service.getBranchName(), comment);
    service.push(false);
    return rev;
  }
  
  /**
   * Clones a repository locally from a status response from a Cadmium site.
   * @param status The status response to clone locally.
   * @return The {@link GitService} object that points to the newly cloned remote repo.
   * @throws Exception
   */
  public static GitService cloneSiteRepo(Status status) throws Exception {
    File tmpDir = File.createTempFile("site", "git");
    GitService git = null;
    if(tmpDir.delete()) {
      try {
        git = GitService.cloneRepo(status.getRepo(), tmpDir.getAbsolutePath());
        if(status.getBranch() != null && !git.getBranchName().equals(status.getBranch())) {
          git.switchBranch(status.getBranch());
        }
        if(status.getRevision() != null && !git.getCurrentRevision().equals(status.getRevision())) {
          git.resetToRev(status.getRevision());
        }
      } catch(Exception e) {
        System.err.println("Failed to clone repo "+status.getRepo()+" branch "+status.getBranch()+ "["+tmpDir+"]");
        e.printStackTrace();
        if(git != null) {
          git.close();
          git = null;
        }
      }
    }
    return git;
  }

  /**
   * The command name for this CLI Command.
   */
  @Override
  public String getCommandName() {
    return "clone";
  }
}
