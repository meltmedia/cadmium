package com.meltmedia.cadmium.cli;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;


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
	
	@Parameter(description="<site> <repo>", required=true)
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
			
		  List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("branch", branch));
		  params.add(new BasicNameValuePair("repo", repo));
		  params.add(new BasicNameValuePair("domain", domain));
		  
 		  post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			
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
		  e.printStackTrace();
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
