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


@Parameters(commandDescription = "Deploys the cadmium war to the specified(domain) server", separators="=")
public class DeployCommand extends AbstractAuthorizedOnly implements CliCommand {

	private final Logger log = LoggerFactory.getLogger(getClass());

	//@Parameter(names="--domain", description="The domain where the cadmium application will be deployed", required=false)
	//private String domain;
	
  //@Parameter(names="--context", description="The context root where the cadmium application will be deployed", required=false)
  //private String context;
	
	//@Parameter(names="--repo", description="The repo from which cadmium will serve content initially", required=true)
	//private String repo;
	
	@Parameter(names="--branch", description="The branch from which cadmium will serve content initially", required=false)
	private String branch;
	
	//@Parameter(description="<site>", required=true)
	//private List<String> site;
	
	@Parameter(description="<repo> <site>", required=true)
  private List<String> parameters;

	//public static final String JERSEY_ENDPOINT = "/deploy";

	public void execute() throws ClientProtocolException, IOException {
	  //if(domain == null && context == null) {
	  //  System.err.println("Please specify either --domain and/or --context");
	  //  System.exit(1);
	  //}
	  if( parameters.size() < 2 ) {
	    System.err.println("The site and repository must be specified.");
	    System.exit(1);
	  }
	  

    String repo = parameters.get(0);
	  String site = parameters.get(1);
	  if( !site.endsWith("/") ) site = site+"/";
	  if( branch == null ) branch = "master";
	  String domain = URI.create(site).getHost();
	  String url = removeSubDomain(site)+"system/deploy";
	  System.out.println(url);
    log.debug("siteUrl + JERSEY_ENDPOINT = {}", url);
	  
		DefaultHttpClient client = new DefaultHttpClient();

		try {
			
			HttpPost post = new HttpPost(url);
			addAuthHeader(post);
			post.setHeader("Content-Type", MediaType.APPLICATION_JSON);
			
			DeployRequest req = new DeployRequest();
			req.setBranch(branch);
			req.setRepo(repo);
			req.setDomain(domain);
		  
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
		  //e.printStackTrace();
			System.err.println("Failed to deploy cadmium application to [" + site + "], with repo [" + repo + "] and branch [" + branch + "]");
			System.exit(1);
		}		

	}

  @Override
  public String getCommandName() {
    return "deploy";
  }
  
  static String removeSubDomain(String url) {
    return url.replaceAll("\\A([^:]+://)[^\\.]+\\.(.*)\\Z", "$1$2");
  }
}
