package com.meltmedia.cadmium.cli;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;


@Parameters(commandDescription = "Deploys the cadmium war to the specified(domain) server", separators="=")
public class DeployCommand implements CliCommand {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Parameter(names="--domain", description="The domain where the cadmium application will be deployed", required=true)
	private String domain;
	
	@Parameter(names="--repo", description="The repo from which cadmium will serve content initially", required=true)
	private String repo;
	
	@Parameter(names="--branch", description="The branch from which cadmium will serve content initially", required=true)
	private String branch;
	
	@Parameter(names="--site", description="The branch from which cadmium will serve content initially", required=true)
	private String site;
	

	public static final String JERSEY_ENDPOINT = "/deploy";

	public void execute() throws ClientProtocolException, IOException {

		DefaultHttpClient client = new DefaultHttpClient();
		String url = site + JERSEY_ENDPOINT;	

		log.info("site + JERSEY_ENDPOINT = {}", url);

		try {
			
			HttpPost post = new HttpPost(url);
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			log.debug("entity content type: {}", entity.getContentType().getValue());
			System.out.println("Successfully deployed cadmium application to [" + domain + "], with repo [" + repo + "] and branch [" + branch + "]");
		} 
		catch (Exception e) {

			System.err.println("Failed to deploy cadmium application to [" + domain + "], with repo [" + repo + "] and branch [" + branch + "]");
		}		

	}

  @Override
  public String getCommandName() {
    return "deploy";
  }

}
