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

import java.io.File;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.meltmedia.cadmium.core.MavenVector;
import com.meltmedia.cadmium.core.WarInfo;
import com.meltmedia.cadmium.core.util.WarUtils;

/**
 * Gets information about a specified war.  Works with remote wars through a deployer.
 * 
 * @author John McEntire
 *
 */
@Parameters(commandDescription="Gets information about a cadmium war.", separators="=")
public class WarInfoCommand extends AbstractAuthorizedOnly implements
    CliCommand {

  @Parameter(names={"--deployer-site","-s"}, description="Deployer url", required=false)
  private String site;
  
  @Parameter(description="<warName>", required=true)
  private List<String> args;

  @Override
  public String getCommandName() {return "war-info";}

  @Override
  public void execute() throws Exception {
    if(args == null || args.size() != 1) {
      System.err.println("Please specify the name of a war.");
      System.exit(1);
    }
    
    String site = null;
    if(this.site != null) {
      site = this.getSecureBaseUrl(this.site);
    }
    String warName = args.get(0);
    
    WarInfo info = null;
    if(site != null){
      info = getDeployedWarInfo(site, warName, token);
    } else {
      info = WarUtils.getWarInfo(new File(warName));
    }
    
    if(info == null) {
      System.out.println("War ["+warName+"] not found.");
    } else {
      System.out.println("War Information["+info.getWarName()+"]");
      System.out.println("  Domain     => \""+info.getDomain()+"\"");
      System.out.println("  Context    => \""+info.getContext()+"\"");
      System.out.println("\nContent: ");
      System.out.println("  Repository => \""+info.getRepo()+"\"");
      System.out.println("  Branch     => \""+info.getContentBranch()+"\"");
      System.out.println("\nConfiguration: ");
      System.out.println("  Repository => \""+info.getConfigRepo()+"\"");
      System.out.println("  Branch     => \""+info.getConfigBranch()+"\"");
      System.out.println("\nArtifact Info:");
      for(MavenVector mvn : info.getArtifacts()) {
        System.out.println("  "+mvn.getGroupId()+":"+mvn.getArtifactId()+":war:"+mvn.getVersion());
      }
    }

  }
  
  /**
   * Retrieves information about a deployed cadmium war.
   *  
   * @param url The uri to a Cadmium deployer war.
   * @param warName The name of a deployed war.
   * @param token The Github API token used for authentication.
   * @return
   * @throws Exception
   */
  public static WarInfo getDeployedWarInfo(String url, String warName, String token) throws Exception {
    DefaultHttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
    
    HttpGet get = new HttpGet(url + "/system/deployment/details/"+warName);
    addAuthHeader(token, get);
    
    HttpResponse resp = client.execute(get);
    
    if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      WarInfo info = new Gson().fromJson(EntityUtils.toString(resp.getEntity()), WarInfo.class);
      return info;
    }
    
    return null;
  }

}
