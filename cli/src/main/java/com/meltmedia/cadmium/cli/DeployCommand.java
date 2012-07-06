package com.meltmedia.cadmium.cli;

import java.io.IOException;
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

	@Parameter(names="--domain", description="The domain where the cadmium application will be deployed", required=false)
	private String domain;
	
  @Parameter(names="--context", description="The context root where the cadmium application will be deployed", required=false)
  private String context;
	
	@Parameter(names="--repo", description="The repo from which cadmium will serve content initially", required=true)
	private String repo;
	
	@Parameter(names="--branch", description="The branch from which cadmium will serve content initially", required=true)
	private String branch;
	
	@Parameter(description="<site>", required=true)
	private List<String> site;

	public static final String JERSEY_ENDPOINT = "/deploy";

	public void execute() throws ClientProtocolException, IOException {
	  if(domain == null && context == null) {
	    System.err.println("Please specify either --domain and/or --context");
	    System.exit(1);
	  }
		DefaultHttpClient client = new DefaultHttpClient();
		String siteUrl = site.get(0);
		String url = siteUrl + JERSEY_ENDPOINT;	

		log.debug("siteUrl + JERSEY_ENDPOINT = {}", url);

		try {
			
			HttpPost post = new HttpPost(url);
			addAuthHeader(post);
			
		  List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("branch", branch));
		  params.add(new BasicNameValuePair("repo", repo));
		  if(domain != null) {
		  params.add(new BasicNameValuePair("domain", domain));
		  }
		  if(context != null) {
		    params.add(new BasicNameValuePair("context", context));
		  }
		  
 		  post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			
			HttpResponse response = client.execute(post);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
  			HttpEntity entity = response.getEntity();
  			String resp = EntityUtils.toString(entity);
  			if(resp.equalsIgnoreCase("ok")) {
    			log.debug("entity content type: {}", entity.getContentType().getValue());
    			System.out.println("Successfully deployed cadmium application to [" + domain + "], with repo [" + repo + "] and branch [" + branch + "]");
  			} else {
  			  throw new Exception("");
  			}
			} else {
        throw new Exception("Bad response status: "+response.getStatusLine().toString());
			}
		} 
		catch (Exception e) {
		  e.printStackTrace();
			System.err.println("Failed to deploy cadmium application to [" + domain + "], with repo [" + repo + "] and branch [" + branch + "]");
			System.exit(1);
		}		

	}

  @Override
  public String getCommandName() {
    return "deploy";
  }

}
