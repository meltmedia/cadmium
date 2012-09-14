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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
	  if( branch == null ) branch = "master";
	  String domain = URI.create(site).getHost();
	  String url = removeSubDomain(site)+"system/deploy";
	  System.out.println(url);
    log.debug("siteUrl + JERSEY_ENDPOINT = {}", url);
    HttpResponse response = null;
		try {
	    DefaultHttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
			
			HttpPost post = new HttpPost(url);
			addAuthHeader(post);
			post.setHeader("Content-Type", MediaType.APPLICATION_JSON);
			
			DeployRequest req = new DeployRequest();
			req.setBranch(branch);
			req.setRepo(repo);
			req.setDomain(domain);
			req.setArtifact(artifact);
		  
 		  post.setEntity(new StringEntity(new Gson().toJson(req), "UTF-8"));
			
			response = client.execute(post);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
			  Header hdr = response.getFirstHeader("Location");
			  post.releaseConnection();
			  if(hdr != null) {
    			log.debug("Location: {}", hdr.getValue());
    		  waitForDeployment(client, site, repo, hdr.getValue());
  			} else {
  			  System.err.println("Error response: "+response);
  			  throw new Exception("Error response: "+response);
  			}
			} else {
        throw new Exception("Bad response status: "+response.getStatusLine().toString());
			}
		} 
		catch (Exception e) {
		  e.printStackTrace();
			System.err.println("Failed to deploy cadmium application to [" + site + "], with repo [" + repo + "] and branch [" + branch + "]");
			System.exit(1);
		}

	}
	
	/**
	 * Waits for deployment to complete or fail.
	 * 
	 * @param client
	 * @param site
	 * @param repo
	 * @param location
	 */
  private void waitForDeployment(HttpClient client, String site, String repo, String location) {
    Map<String, Integer> lastLogIndexes = new HashMap<String, Integer>();
    long startTime = System.currentTimeMillis();
    long timeoutTime = startTime + (5 * 60 * 1000l);
    while(System.currentTimeMillis() < timeoutTime) {
      String response = null;
      try {
        HttpGet get = new HttpGet(location);
        HttpResponse resp = client.execute(get);
        int statusCode = resp.getStatusLine().getStatusCode();
        response = EntityUtils.toString(resp.getEntity());
        get.releaseConnection();
        if(statusCode == HttpStatus.SC_ACCEPTED) {
          updateMessages(lastLogIndexes, new Gson().fromJson(response, DeploymentStatus.class));
          Thread.sleep(5000l);
        } else if(statusCode == HttpStatus.SC_OK) {
          System.out.println("Successfully deployed cadmium application to [" + site + "], with repo [" + repo + "] and branch [" + branch + "]");
          break;
        } else if(statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
          System.err.println("Failed to deploy to [" + site + "], with repo [" + repo + "], and branch [" + branch + "]:\n" + response);
          System.exit(1);
        }
      } catch(Throwable t) {
        throw new RuntimeException("Failed to check status of deployment["+response+"].", t);
      }
    }
    
  }

  /**
   * Displays any new messages that have been replied with.
   * 
   * @param lastLogIndexes
   * @param status
   */
  private void updateMessages(Map<String, Integer> lastLogIndexes,
      DeploymentStatus status) {
    if(status != null && status.memberLogs != null) {
      for(String member : status.memberLogs.keySet()) {
        int i = getLastIndex(member, lastLogIndexes);
        List<String> memLogs = status.memberLogs.get(member);
        if(memLogs.size() > i) {
          for(; i < memLogs.size(); i = incLastIndex(member, lastLogIndexes, i)) {
            System.out.println("["+member+"] "+memLogs.get(i));
          }
        }
      }
    }
  }

  private int incLastIndex(String member,
      Map<String, Integer> lastLogIndexes, int i) {
    i++;
    lastLogIndexes.put(member, i);
    return i;
  }

  /**
   * @param member
   * @param lastLogIndexes
   * @return Gets the last log index for the given member.
   */
  private int getLastIndex(String member,
      Map<String, Integer> lastLogIndexes) {
    if(lastLogIndexes.containsKey(member)) {
      return lastLogIndexes.get(member);
    }
    return 0;
  }

  @Override
  public String getCommandName() {
    return "deploy";
  }
  
  /**
   * Removes the sub-domain of the passed in url to get the url of the deployer instance.
   * 
   * @param url
   * @return
   */
  static String removeSubDomain(String url) {
    return url.replaceAll("\\A([^:]+://)[^\\.]+\\.(.*)\\Z", "$1$2");
  }
}
