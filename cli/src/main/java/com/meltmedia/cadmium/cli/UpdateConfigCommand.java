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
import com.meltmedia.cadmium.core.api.UpdateRequest;
import com.meltmedia.cadmium.status.Status;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Sends a raw update config command to a Cadmium site.
 * 
 * @author Brian Barr
 *
 */
@Parameters(commandDescription = "Instructs a site to update its configuration.", separators="=")
public class UpdateConfigCommand extends AbstractAuthorizedOnly implements CliCommand {

  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String UPDATE_CONFIG_ENDPOINT = "/system/update/config";

  @Parameter(names={"--repo"}, description="A new git repository url to switch to.", required=false)
  private String repo;

  @Parameter(names={"--branch", "-b"}, description="The branch that you are updating to", required=false)
  private String branch;

  @Parameter(names={"--revision", "-r"}, description="The revision that you are updating to", required=false)
  private String revision;

  @Parameter(description="<site>", required=true)
  private List<String> site;

  @Parameter(names={"--message", "-m"}, description="The comment for the history", required=true)
  private String message;

  @Parameter(names={"--force", "-f"}, description="Force the update", required=false)
  private boolean force;

  public void execute() throws ClientProtocolException, IOException {

    String siteUrl = getSecureBaseUrl(site.get(0));

    //GitService gitValidation = null;
    try {

      if(!UpdateCommand.isValidBranchName(branch, UpdateRequest.CONFIG_BRANCH_PREFIX)) {
        System.exit(1);
      }
      
      System.out.println("Getting status of ["+ siteUrl +"]");
      
      Status siteStatus = StatusCommand.getSiteStatus(siteUrl, token);

      boolean repoSame = false;
      boolean branchSame = false;
      boolean revisionSame = false;
      boolean forceUpdate = force;      

      String currentConfigRepo = siteStatus.getConfigRepo();
      String currentConfigRevision = siteStatus.getConfigRevision();
      String currentConfigBranch = siteStatus.getConfigBranch();

      log.debug("config repo = {}, and currentConfigRepo = {}", repo, currentConfigRepo);

      if(repo != null && repo.trim().equals(currentConfigRepo.trim())) {

        repoSame = true;
      }

      log.debug("branch = {}, and currentBranch = {}", branch, currentConfigBranch);

      if(branch != null && branch.trim().equals(currentConfigBranch.trim())) {

        branchSame = true;
      }

      log.debug("revision = {}, and currentRevision = {}", revision, currentConfigRevision);

      if(revision != null && revision.trim().equals(currentConfigRevision.trim())) {

        revisionSame = true;
      }

      log.debug("branchSame = {}, and revisionSame = {}", branchSame, revisionSame);

      if(repoSame && branchSame && revisionSame && !forceUpdate) {

        System.out.println("The config for site [" + siteUrl  + "] is already on repo [" + repo + "] branch [" + branch + "] and revision [" + revision  + "].");
      }
      else {    
        /*if(repo != null) {
          siteStatus.setRepo(repo);
          siteStatus.setBranch(null);
          siteStatus.setRevision(null);
        }*/
        /*if(repo == null) {
          repo = siteStatus.getRepo();
          siteStatus.setRepo(siteStatus.getConfigRepo());
          siteStatus.setBranch(siteStatus.getConfigBranch());
          siteStatus.setRevision(siteStatus.getConfigRevision());
        }*/
        //gitValidation = CloneCommand.cloneSiteRepo(siteStatus);
        String newBranch = null;
        String rev = null;

        /*if(branch == null || branch.length() == 0) {
          gitValidation.switchBranch(siteStatus.getConfigBranch());
        }*/
        if(branch != null) {
          //if(gitValidation.isBranch(branch)) {
          newBranch = branch;
          //  gitValidation.switchBranch(branch);
          log.debug("branch being added = {}", branch);
          //} else {
          //  System.err.println("The branch ["+branch+"] does not exist.");
          //  throw new Exception("");
          //}
        }

        if(revision != null) {
          //if(gitValidation.checkRevision(revision)){
          rev = revision;
          log.debug("revision being added = {}", revision);
          //} else {
          //  System.err.println("Revision ["+revision+"] does not exist on the branch ["+gitValidation.getBranchName()+"]");
          //  throw new Exception("");
          //}
        } 

        if(UpdateCommand.sendUpdateMessage(siteUrl, repo, newBranch, rev, message, token, UPDATE_CONFIG_ENDPOINT, UpdateRequest.CONFIG_BRANCH_PREFIX)){
          System.out.println("Update successful");
        }

      }

    } 
    catch (Exception e) {

      System.err.println("Failed to updated site [" + siteUrl  + "] to repo [" + repo + "] branch [" + branch  + "] and revision [" + revision  + "].");
    } /* finally {
      if(gitValidation != null) {
        try {
          FileSystemManager.deleteDeep(gitValidation.getBaseDirectory());
        } catch (Exception e) {
        }
      }
    }*/

  }

  @Override
  public String getCommandName() {
    return "update-config";
  }
  
}
