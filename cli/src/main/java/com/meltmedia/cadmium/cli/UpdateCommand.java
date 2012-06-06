package com.meltmedia.cadmium.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.status.Status;
import com.meltmedia.cadmium.status.StatusMember;

@Parameters(commandDescription = "Instructs a site to update its content.")
public class UpdateCommand {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Parameter(names="branch", description="The branch that you are updating", required=true)
	private String branch;

	@Parameter(names="revision", description="The revision that you are updating to", required=true)
	private String revision;

	@Parameter(names="site", description="The site that is to be updated", required=true)
	private String site;

	@Parameter(names="comment", description="The comment for the history", required=true)
	private String comment;

	@Parameter(names="--force", description="The comment for the history", required=false)
	private String force;

	public static final String JERSEY_ENDPOINT = "/system/update";

	public void execute() throws ClientProtocolException, IOException {

		DefaultHttpClient client = new DefaultHttpClient();
		String url = site + JERSEY_ENDPOINT;	

		log.debug("site + JERSEY_ENDPOINT = {}", url);


		System.out.println("Getting status of ["+site+"]");
		try {

			Status siteStatus = CloneCommand.getSiteStatus(site);

			boolean branchSame = true;
			boolean revisionSame = true;
			
			String currentRevision = siteStatus.getRevision();
			String currentBranch = siteStatus.getBranch();


			if(!branch.trim().equals(currentBranch.trim())) {
				//update the branch to requested branch
				branch = currentBranch.trim();
				branchSame = false;
			}
						
			if(revision.trim().equals(currentRevision.trim())) {
				//update the branch to requested branch
				revision = currentRevision.trim();
				revisionSame = false;
			}

			if( branchSame && revisionSame) {
				
				System.out.println("The site [" + site  + "] is already on branch [" + branch  + "] and revision [" + revision  + "].");
			}
			else {				

				HttpPost post = new HttpPost(url);
				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair("branch", branch.trim()));
				nvps.add(new BasicNameValuePair("rev", revision.trim()));
				nvps.add(new BasicNameValuePair("comment", comment.trim()));
	
				post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
				HttpResponse response = client.execute(post);
				System.out.println(response.toString());	
			}

		} 
		catch (Exception e) {

			System.err.println("Failed to updated site [" + site  + "] to branch [" + branch  + "] and revision [" + revision  + "].");
		}




	}
}
