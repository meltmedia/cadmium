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
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.meltmedia.cadmium.core.api.DeployRequest;
import com.meltmedia.cadmium.core.api.UpdateRequest;

/**
 * This command is used to tell a Cadmium-Deployer instance to create and deploy a new Cadmium war.
 * 
 * 
 * @author Brian Barr
 * @author John McEntire
 * @author Christian Trimble
 *
 */
@Parameters(commandDescription = "Deploys the cadmium war to the specified(domain) server", separators="=")
public class DeployCommand extends AbstractAuthorizedOnly implements CliCommand {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Parameter(names="--branch", description="The branch from which cadmium will serve content initially", required=false)
	private String branch;
  
  @Parameter(names={"--configuration-branch", "-C"}, description="The branch from which cadmium will load configuration from initially", required=false)
  private String configBranch;
  
  @Parameter(names="--configuration-repo", description="The git repository uri to load the configuration from. If not specified the configuration will load from the same repo as content.", required=false)
  private String configRepo;
  
  @Parameter(names="--artifact", description="The maven coordinates to a cadmium war.", required=false)
  private String artifact;
	
	@Parameter(description="<repo> <site>", required=true)
  private List<String> parameters;

	/**
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void execute() throws ClientProtocolException, IOException {
	  if( parameters.size() < 2 ) {
	    System.err.println("The site and repository must be specified.");
	    System.exit(1);
	  }
	  

    String repo = parameters.get(0);
	  String site = getSecureBaseUrl(parameters.get(1));
	  if( !site.endsWith("/") ) site = site+"/";
	  if( StringUtils.isBlank(branch) ) branch = UpdateRequest.CONTENT_BRANCH_PREFIX+"-master";
    if( StringUtils.isBlank(configBranch) ) configBranch = UpdateRequest.CONFIG_BRANCH_PREFIX +"-master";
	  boolean validRequest = true;
	  if( !StringUtils.startsWithIgnoreCase(branch, UpdateRequest.CONTENT_BRANCH_PREFIX + "-" )) {
	    validRequest = false;
	    System.err.println("Content branch must start with \""+UpdateRequest.CONTENT_BRANCH_PREFIX+"-\"");
	  }
    if( !StringUtils.startsWithIgnoreCase(configBranch, UpdateRequest.CONFIG_BRANCH_PREFIX + "-" )) {
      validRequest = false;
      System.err.println("Configuration branch must start with \""+UpdateRequest.CONFIG_BRANCH_PREFIX+"-\"");
    }
    if(!validRequest) {
      System.exit(1);
    }
	  String domain = URI.create(site).getHost();
	  String url = removeSubDomain(site)+"system/deploy";
	  System.out.println(url);
    log.debug("siteUrl + JERSEY_ENDPOINT = {}", url);

		try {
	    DefaultHttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
			
			HttpPost post = new HttpPost(url);
			addAuthHeader(post);
			post.setHeader("Content-Type", MediaType.APPLICATION_JSON);
			
			DeployRequest req = new DeployRequest();
			req.setBranch(branch);
      req.setConfigBranch(configBranch);
			req.setRepo(repo);
      req.setConfigRepo(StringUtils.isBlank(configRepo) ? repo : configRepo);
			req.setDomain(domain);
			req.setArtifact(artifact);
		  
 		  post.setEntity(new StringEntity(new Gson().toJson(req), "UTF-8"));
			
			HttpResponse response = client.execute(post);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
  			HttpEntity entity = response.getEntity();
  			String resp = EntityUtils.toString(entity);
  			if(resp.equalsIgnoreCase("ok")) {
    			log.debug("entity content type: {}", entity.getContentType().getValue());
    			System.out.println("Successfully deployed cadmium application to [" + site + "], with repo [" + repo + "] and branch [" + branch + "]");
  			} else {
  			  throw new Exception("");
  			}
			} else {
        throw new Exception("Bad response status: "+response.getStatusLine().toString());
			}
		} 
		catch (Exception e) {
			System.err.println("Failed to deploy cadmium application to [" + site + "], with repo [" + repo + "] and branch [" + branch + "]");
			System.exit(1);
		}		

	}
	
  @Override
  public String getCommandName() {
    return "deploy";
  }
  
  /**
   * Removes the subdomain of the passed in url to get the url of the deployer instance.
   * 
   * @param url
   * @return
   */
  static String removeSubDomain(String url) {
    return url.replaceAll("\\A([^:]+://)[^\\.]+\\.(.*)\\Z", "$1$2");
  }
}
