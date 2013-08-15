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
import com.meltmedia.cadmium.core.MavenVector;
import com.meltmedia.cadmium.status.Status;
import com.meltmedia.cadmium.status.StatusMember;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Displays the status information from a Cadmium site.
 * 
 * @author Brian Barr
 * @author John McEntire
 *
 */
@Parameters(commandDescription = "Displays status info for a site")
public class StatusCommand extends AbstractAuthorizedOnly implements CliCommand {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Parameter(description="<site>", required=true)
	private List<String> site;	

	public static final String JERSEY_ENDPOINT = "/system/status";

	public void execute() throws Exception {
	  if(site.size() != 1) {
	    System.err.println("Please specify a url");
	    System.exit(1);
	  }
	  String siteUrl = getSecureBaseUrl(site.get(0));
		Status statusObj = getSiteStatus(siteUrl, token);
		if(statusObj != null) {
      List<StatusMember> members = statusObj.getMembers();
      
      log.debug(statusObj.toString());              
     
      System.out.println();
      System.out.println("Current status for [" + siteUrl +"]"); 
      System.out.println("-----------------------------------------------------");
      System.out.print(
      		"Environment      => [" + statusObj.getEnvironment() + "]\n" +
      		"Repo URL         => [" + statusObj.getRepo() + "]\n" +
      		"Branch           => [" + statusObj.getBranch() + "]\n" +
      		"Revision         => [" + statusObj.getRevision() + "]\n" +
          "Config Repo URL  => [" + statusObj.getConfigRepo() + "]\n" +
          "Config Branch    => [" + statusObj.getConfigBranch() + "]\n" +
          "Config Revision  => [" + statusObj.getConfigRevision() + "]\n" +
      		"Content Source   => [\n" + statusObj.getSource() + "]\n" +
      		"Maint Page State => [" + statusObj.getMaintPageState() +"]\n");
      if(StringUtils.isNotBlank(statusObj.getCadmiumVersion())) {
        System.out.println(
          "Cadmium Version  => [" + statusObj.getCadmiumVersion() + "]\n"
        );
      } else {
        System.out.println();
      }
      
      System.out.println();
      System.out.println("Member States:\n");
      System.out.println("-----------------------------------------------------");
      for(StatusMember member : members) {
      	System.out.println(
      	    "   External IP     : [" + member.getExternalIp() +"]\n" +
      			"   Address         : [" + member.getAddress() + "]\n" +
      			"   Is Coordinator? : [" + member.isCoordinator() + "]\n" +
      			"   State           : [" + member.getState() + "]\n" +
            "   Config State    : [" + member.getConfigState() + "]\n" +
      			"   Is Me?          : [" + member.isMine() + "]");
      	if(member.getWarInfo() != null) {
        	System.out.println(
              "   War File Name   : [" + member.getWarInfo().getWarName() + "]\n" +
        			"   Domain          : [" + member.getWarInfo().getDomain() + "]\n" +
              "   Context         : [" + member.getWarInfo().getContext() + "]\n" +
        			"   Content Repo    : [" + member.getWarInfo().getRepo() + "]\n" +
              "   Content Branch  : [" + member.getWarInfo().getContentBranch() + "]\n" +
        			"   Config Repo     : [" + member.getWarInfo().getConfigRepo() + "]\n" +
              "   Config Branch   : [" + member.getWarInfo().getConfigBranch() + "]"
        	);
        	if(member.getWarInfo().getArtifacts() != null) {
        	  System.out.print("   Artifact        : [");
        	  boolean first = true;
        	  for(MavenVector mvn : member.getWarInfo().getArtifacts()) {
        	    System.out.print((!first?", ":"") + mvn.getGroupId()+":"+mvn.getArtifactId()+":war:"+mvn.getVersion());
        	    first = false;
        	  }
        	  System.out.println("]");
        	}
      	}
      	System.out.println();
      }
		} else {
		  System.out.println("No status returned.");
		}
			
	}

  @Override
  public String getCommandName() {
    return "status";
  }

  /**
   * Retrieves the status of a Cadmium site into a {@link Status} Object.
   * 
   * @param site The site uri of the Cadmium site to get the status from.
   * @param token The Github API token used for authenticating the request.
   * @return
   * @throws Exception
   */
  public static Status getSiteStatus(String site, String token) throws Exception {
    HttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
    
    HttpGet get = new HttpGet(site + StatusCommand.JERSEY_ENDPOINT);
    addAuthHeader(token, get);
    
    HttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    if(entity.getContentType().getValue().equals(MediaType.APPLICATION_JSON)) { 
      String responseContent = EntityUtils.toString(entity);            
      return new Gson().fromJson(responseContent, new TypeToken<Status>() {}.getType());
    }
    return null;
  }

}
