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

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.api.BasicApiResponse;
import com.meltmedia.cadmium.core.api.UpdateRequest;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.status.Status;

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

    System.out.println("Getting status of ["+ siteUrl +"]");
    GitService gitValidation = null;
    try {

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
        if(repo != null) {
          siteStatus.setRepo(repo);
          siteStatus.setBranch(null);
          siteStatus.setRevision(null);
        }
        if(repo == null) {
          repo = siteStatus.getRepo();
        }
        gitValidation = CloneCommand.cloneSiteRepo(siteStatus);
        String newBranch = null;
        String rev = null;

        if(branch == null || branch.length() == 0) {
          gitValidation.switchBranch(siteStatus.getBranch());
        }
        if(branch != null) {
          if(gitValidation.isBranch(branch)) {
            newBranch = branch;
            gitValidation.switchBranch(branch);
            log.debug("branch being added = {}", branch);
          } else {
            System.err.println("The branch ["+branch+"] does not exist.");
            throw new Exception("");
          }
        }

        if(revision != null) {
          if(gitValidation.checkRevision(revision)){
            rev = revision;
            log.debug("revision being added = {}", revision);
          } else {  
            System.err.println("Revision ["+revision+"] does not exist on the branch ["+gitValidation.getBranchName()+"]");
            throw new Exception("");
          }
        } 

        if(sendUpdateConfigMessage(siteUrl, repo, newBranch, rev, message, token)){
          System.out.println("Update successful");
        }

      }

    } 
    catch (Exception e) {

      System.err.println("Failed to updated site [" + siteUrl  + "] to repo [" + repo + "] branch [" + branch  + "] and revision [" + revision  + "].");
    } finally {
      if(gitValidation != null) {
        try {
          FileSystemManager.deleteDeep(gitValidation.getBaseDirectory());
        } catch (Exception e) {
        }
      }
    }

  }

  @Override
  public String getCommandName() {
    return "update";
  }

  /**
   * Sends a update config message to a Cadmium site. This method will block until the update is complete.
   * 
   * @param site2 The uri to a Cadmium site.
   * @param repo The git repository to tell the site to change to.
   * @param branch The branch to switch to.
   * @param revision The revision to reset to.
   * @param comment The message to record with this event in the history on the Cadmium site.
   * @param token The Github API token to authenticate with.
   * @return true if successfull or false otherwise.
   * @throws Exception
   */
  public static boolean sendUpdateConfigMessage(String site2, String repo, String branch, String revision, String comment, String token) throws Exception {
    HttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());

    HttpPost post = new HttpPost(site2 + UPDATE_CONFIG_ENDPOINT);
    addAuthHeader(token, post);

    post.addHeader("Content-Type", MediaType.APPLICATION_JSON);

    UpdateRequest req = new UpdateRequest();

    if(repo != null) {
      req.setRepo(repo);
    }

    if(branch != null) {
      req.setBranch(branch);
    }

    if(revision != null) {
      req.setSha(revision);
    }

    req.setComment(comment);

    post.setEntity(new StringEntity(new Gson().toJson(req), "UTF-8"));

    HttpResponse response = client.execute(post);

    if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      String responseString = EntityUtils.toString(response.getEntity());
      BasicApiResponse resp = new Gson().fromJson(responseString, BasicApiResponse.class);
      if(!"ok".equals(resp.getMessage())) {
        System.err.println("Update Config message to ["+site2+"] failed. ["+resp.getMessage()+"]");
      } else {
        if(resp.getUuid() != null) {
          HistoryCommand.waitForToken(site2, resp.getUuid(), resp.getTimestamp(), 600000l);
        }
        return true;
      }
    }
    return false;
  }
}
