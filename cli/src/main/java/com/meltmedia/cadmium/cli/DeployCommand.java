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
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.api.DeployRequest;
import com.meltmedia.cadmium.core.api.UpdateRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

  @Parameter(names="--disable-security", hidden=true)
  private boolean disableSecurity = false;
	
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
    String warName = null;
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
      req.setDisableSecurity(disableSecurity);
		  
 		  post.setEntity(new StringEntity(new Gson().toJson(req), "UTF-8"));
			
			HttpResponse response = client.execute(post);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
  			HttpEntity entity = response.getEntity();
  			String resp = EntityUtils.toString(entity);
        try {
          post.releaseConnection();
        } catch(Exception e) {
          log.warn("Failed to release connection.", e);
        }
        if(!resp.equals("ok") && canCheckWar(resp, url, client)) {
          System.out.println("Waiting for Jboss to deploy new war: "+resp);
          warName = resp;
          int secondsToWait = 120;
          while(secondsToWait-- > 0) {
            Thread.sleep(5000l);
            if(checkWarDeployment(warName, url, client)) {
              break;
            }
          }
          if(secondsToWait < 0) {
            System.err.println("Timeout: Deployment of cadmium application to [" + site + "], with repo [" + repo + "] and branch [" + branch + "]");
            System.exit(1);
          }
        } else {
          System.out.println("Deployer not compatible with deployment waiting.");
        }
        log.debug("entity content type: {}", entity.getContentType().getValue());
        System.out.println("Successfully deployed cadmium application to [" + site + "], with repo [" + repo + "] and branch [" + branch + "]");
			} else {
        throw new Exception("Bad response status: "+response.getStatusLine().toString());
			}
		} 
		catch (Exception e) {
			System.err.println("Failed to deploy cadmium application to [" + site + "], with repo [" + repo + "] and branch [" + branch + "]");
      if(warName != null) {
        try {
          System.err.println("Attempting to undeploy partial deployment of "+warName+".");
          UndeployCommand.undeploy(removeSubDomain(site), warName, token);
          System.err.println("");
        } catch(Exception e1){
          System.err.println("Failed to undeploy partial deployment.");
        }
      }
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

  /**
   * Checks via an http options request that the endpoint exists to check for deployment state.
   * @param warName
   * @param url
   * @param client
   * @return
   */
  public boolean canCheckWar(String warName, String url, DefaultHttpClient client) {
    HttpOptions opt = new HttpOptions(url + "/" + warName);
    try {
      HttpResponse response = client.execute(opt);
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        Header allowHeader[] = response.getHeaders("Allow");
        for(Header allow : allowHeader) {
          List<String> values = Arrays.asList(allow.getValue().toUpperCase().split(","));
          if(values.contains("GET")) {
            return true;
          }
        }
      }
      EntityUtils.consumeQuietly(response.getEntity());
    } catch (Exception e) {
      log.warn("Failed to check if endpoint exists.", e);
    } finally {
      opt.releaseConnection();
    }
    return false;
  }

  private boolean started = false;

  public boolean checkWarDeployment(String warName, String url, DefaultHttpClient client) throws Exception {
    HttpGet get= new HttpGet(url + "/" + warName);
    try {
      HttpResponse response = client.execute(get);
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        Map<String, Object> responseObj = new Gson().fromJson(EntityUtils.toString(response.getEntity()), new TypeToken<Map<String,Object>>(){}.getType());
        if(responseObj.get("errors") != null && responseObj.get("errors") instanceof List) {
          List<String> errors = (List<String>)responseObj.get("errors");
          System.err.println("The following nodes have failed:");
          for(String node: errors) {
            System.err.println("  "+node);
          }
          throw new Exception();
        }
        if(!started && (Boolean)responseObj.get("started")) {
          started = true;
          System.out.println("The server has began to deploy.");
        } else if(started && !((Boolean)responseObj.get("started"))) {
          System.err.println("The server has stopped deploying the new war.");
          throw new Exception();
        }
        if((Boolean)responseObj.get("finished")) {
          return true;
        }
      } else {
        EntityUtils.consumeQuietly(response.getEntity());
        System.err.println("Failed to wait for war to deploy.");
        throw new Exception();
      }
    } finally {
      get.releaseConnection();
    }
    return false;
  }

}
