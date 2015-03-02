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
import com.google.gson.Gson;
import com.meltmedia.cadmium.core.api.BasicApiResponse;
import com.meltmedia.cadmium.core.api.UpdateRequest;
import com.meltmedia.cadmium.status.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

/**
 * Sends a raw update command to a Cadmium site.
 * 
 * @author Brian Barr
 * @author John McEntire
 * @author Christian Trimble
 *
 */
@Parameters(commandDescription = "Instructs a site to update its content.", separators="=")
public class UpdateCommand extends AbstractAuthorizedOnly implements CliCommand {

	private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String UPDATE_ENDPOINT = "/system/update";

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
		try {

      if(!isValidBranchName(branch, UpdateRequest.CONTENT_BRANCH_PREFIX)) {
        System.exit(1);
      }

      System.out.println("Getting status of ["+ siteUrl +"]");
			Status siteStatus = StatusCommand.getSiteStatus(siteUrl, token);

			boolean repoSame = false;
			boolean branchSame = false;
			boolean revisionSame = false;
			boolean forceUpdate = force;
			
			String currentRepo = siteStatus.getRepo();
			String currentRevision = siteStatus.getRevision();
			String currentBranch = siteStatus.getBranch();

      log.debug("repo = {}, and currentRepo = {}", repo, currentRepo);
      
      if(repo != null && repo.trim().equals(currentRepo.trim())) {
                
        repoSame = true;
      }

			log.debug("branch = {}, and currentBranch = {}", branch, currentBranch);
			
			if(branch != null && branch.trim().equals(currentBranch.trim())) {
								
				branchSame = true;
			}

			log.debug("revision = {}, and currentRevision = {}", revision, currentRevision);

			if(revision != null && revision.trim().equals(currentRevision.trim())) {
							
				revisionSame = true;
			}

			log.debug("branchSame = {}, and revisionSame = {}", branchSame, revisionSame);

			if(repoSame && branchSame && revisionSame && !forceUpdate) {

				System.out.println("The site [" + siteUrl  + "] is already on repo [" + repo + "] branch [" + branch + "] and revision [" + revision  + "].");
			}
			else {
				String newBranch = null;
				String rev = null;
        if(branch != null) {
          newBranch = branch;
          log.debug("branch being added = {}", branch);
        }

        if(revision != null) {
          rev = revision;
          log.debug("revision being added = {}", revision);
				}
				
				if(sendUpdateMessage(siteUrl, repo, newBranch, rev, message, token)){
				  System.out.println("Update successful");
				}

			}

		} 
		catch (Exception e) {

			System.err.println("Failed to updated site [" + siteUrl  + "] to repo [" + repo + "] branch [" + branch  + "] and revision [" + revision  + "].");
		}

	}

	@Override
	public String getCommandName() {
		return "update";
	}
	

  
  /**
   * Sends a update message to a Cadmium site. This method will block until the update is complete.
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
  public static boolean sendUpdateMessage(String site2, String repo, String branch, String revision, String comment, String token) throws Exception {
    return sendUpdateMessage(site2, repo, branch, revision, comment, token, UPDATE_ENDPOINT);
  }
  
  /**
   * Sends a update message to a Cadmium site. This method will block until the update is complete.
   * 
   * @param site2 The uri to a Cadmium site.
   * @param repo The git repository to tell the site to change to.
   * @param branch The branch to switch to.
   * @param revision The revision to reset to.
   * @param comment The message to record with this event in the history on the Cadmium site.
   * @param token The Github API token to authenticate with.
   * @param endpoint The endpoint to send the update request to.
   * @return true if successfull or false otherwise.
   * @throws Exception
   */
  public static boolean sendUpdateMessage(String site2, String repo, String branch, String revision, String comment, String token, String endpoint) throws Exception {
    return sendUpdateMessage(site2, repo, branch, revision, comment, token, endpoint, UpdateRequest.CONTENT_BRANCH_PREFIX);
  }
  
	/**
	 * Sends a update message to a Cadmium site. This method will block until the update is complete.
	 * 
	 * @param site2 The uri to a Cadmium site.
	 * @param repo The git repository to tell the site to change to.
	 * @param branch The branch to switch to.
	 * @param revision The revision to reset to.
	 * @param comment The message to record with this event in the history on the Cadmium site.
	 * @param token The Github API token to authenticate with.
	 * @param endpoint The endpoint to send the update request to.
	 * @param branchPrefix The branch prefix for the given update command.
	 * @return true if successfull or false otherwise.
	 * @throws Exception
	 */
  public static boolean sendUpdateMessage(String site2, String repo, String branch, String revision, String comment, String token, String endpoint, String branchPrefix) throws Exception {
    HttpClient client = httpClient();
    
    HttpPost post = new HttpPost(site2 + endpoint);
    addAuthHeader(token, post);
    
    post.addHeader("Content-Type", MediaType.APPLICATION_JSON);
    
    UpdateRequest req = new UpdateRequest();
    
    if(repo != null) {
      req.setRepo(repo);
    }
    
    if(branch != null && isValidBranchName(branch, branchPrefix)) {
      req.setBranch(branch);
    } else if(branch != null) {
      throw new Exception("Branch name must be prefixed with "+branchPrefix+".");
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
        System.err.println("Update message to ["+site2+"] failed. ["+resp.getMessage()+"]");
      } else {
        if(resp.getUuid() != null) {
          HistoryCommand.waitForToken(site2, resp.getUuid(), resp.getTimestamp(), 600000l);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Validates the branch name that will be sent to the given prefix.
   * 
   * @param branch 
   * @param prefix
   * @return
   */
  public static boolean isValidBranchName(String branch, String prefix) throws Exception {
    if(StringUtils.isNotBlank(prefix) && StringUtils.isNotBlank(branch)) {
      if(StringUtils.startsWithIgnoreCase(branch, prefix + "-")) {
        return true;
      } else {
        System.err.println("Branch name must start with prefix \""+prefix+"-\".");
      }
    } else {
      return true;
    }
    return false;
  }
}
