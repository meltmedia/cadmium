package com.meltmedia.cadmium.cli;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.status.Status;


@Parameters(commandDescription = "Displays status info for a site", separators="=")
public class StatusCommand {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Parameter(names="--site", description="The site for which the status is desired", required=true)
	private String site;	

	public static final String JERSEY_ENDPOINT = "/system/status";

	public void execute() throws ClientProtocolException, IOException {

		DefaultHttpClient client = new DefaultHttpClient();
		String url = site + JERSEY_ENDPOINT;	
		
		log.debug("site + JERSEY_ENDPOINT = {}", url);
		
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		
		log.debug("entity content type: {}", entity.getContentType().getValue());
		if(entity.getContentType().getValue().equals("application/json")) {			
		
			log.debug("statusObj.toString()");
			
            String responseContent = EntityUtils.toString(entity);            
            Status statusObj = new Gson().fromJson(responseContent, new TypeToken<Status>() {}.getType());
            
            log.debug("statusObj.toString()");
            System.out.println(statusObj.toString());
		}		
			
	}

}
