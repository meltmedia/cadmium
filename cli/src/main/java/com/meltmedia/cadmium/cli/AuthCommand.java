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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.crypto.hash.format.DefaultHashFormatFactory;
import org.apache.shiro.crypto.hash.format.HashFormat;
import org.apache.shiro.crypto.hash.format.HashFormatFactory;
import org.apache.shiro.util.ByteSource;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * CLI command to manager site specific authentication.
 * 
 * @author John McEntire
 *
 */
@Parameters(commandDescription = "Manages site access", separators="=")
public class AuthCommand extends AbstractAuthorizedOnly implements CliCommand {
  
  public static final String SERVICE_PATH = "/system/auth";
  
  @Parameter(description="<action:(list|add|remove)> <site>", arity=2, required=true)
  private List<String> args;
  
  @Parameter(names={"-u","--username"})
  private String username;
  
  @Parameter(names={"-p","--password"})
  private String password;
  
  public static final String COMMAND = "auth";
  
  @Override
  public String getCommandName() { return COMMAND; }

  @Override
  public void execute() throws Exception {
    if(args == null || args.size() != 2) {
      System.err.println("Please specify action (list|add|remove) and site.");
      System.exit(1);
    }
    String action = args.get(0);
    String site = this.getSecureBaseUrl(args.get(1)+SERVICE_PATH);
    HttpUriRequest request = null;
    int expectedStatus = HttpStatus.SC_OK;
    if(action.equalsIgnoreCase("list")) {
      System.out.println("Listing site ("+args.get(1)+") specific users.");
      request = new HttpGet(site);
    } else if(action.equalsIgnoreCase("add")){
      if( StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) ) {
        expectedStatus = HttpStatus.SC_CREATED;
        String passwordHash = hashPasswordForShiro();
        System.out.println("Adding User ["+username+"] with password hash ["+passwordHash+"]");
        request = new HttpPut(site + "/" + username);
        ((HttpPut)request).setEntity(new StringEntity(passwordHash));
      } else {
        System.err.println("Both username and password are required to add a user.");
        System.exit(1);
      }
    } else if(action.equalsIgnoreCase("remove")){
      if(StringUtils.isNotBlank(username)) {
        expectedStatus = HttpStatus.SC_GONE;
        System.out.println("Removing User ["+username+"]");
        request = new HttpDelete(site + "/" + username);
      } else {
        System.err.println("The username of the user to remove is required.");
        System.exit(1);
      }
    }
    sendRequest(request, expectedStatus);
  }

  /**
   * Sends a request to the rest endpoint.
   * 
   * @param request
   * @param expectedStatus
   * @throws KeyManagementException
   * @throws UnrecoverableKeyException
   * @throws NoSuchAlgorithmException
   * @throws KeyStoreException
   * @throws IOException
   * @throws ClientProtocolException
   */
  private void sendRequest(HttpUriRequest request, int expectedStatus)
      throws KeyManagementException, UnrecoverableKeyException,
      NoSuchAlgorithmException, KeyStoreException, IOException,
      ClientProtocolException {
    addAuthHeader(request);
    
    DefaultHttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
    
    HttpResponse response = client.execute(request);
    if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
      EntityUtils.consume(response.getEntity());
      System.err.println("Authentication has been disabled for "+args.get(1)+".");
    } else if(response.getStatusLine().getStatusCode() == expectedStatus) {
      if(expectedStatus == HttpStatus.SC_OK) {
        String responseStr = EntityUtils.toString(response.getEntity());
        List<String> usernames = new Gson().fromJson(responseStr, new TypeToken<List<String>>(){}.getType());
        if(usernames == null || usernames.isEmpty()) {
          System.out.println("There have been no site specific users created.");
        } else {
          System.out.println(usernames.size() + " Users:");
          for(String user : usernames) {
            System.out.println(user);
          }
        }
      }
    } else {
      System.err.println(EntityUtils.toString(response.getEntity()));
      System.err.println("Unexpected status code returned. "+response.getStatusLine().getStatusCode());
    }
  }

  /**
   * Hashes a password the shiro way.
   * @return
   */
  private String hashPasswordForShiro() {
    //Hash password
    HashFormatFactory HASH_FORMAT_FACTORY = new DefaultHashFormatFactory();
    SecureRandomNumberGenerator generator = new SecureRandomNumberGenerator();
    int byteSize = 128 / 8; 
    ByteSource salt = generator.nextBytes(byteSize);
    
    SimpleHash hash = new SimpleHash("SHA-256", password, salt, 500000);
    
    HashFormat format = HASH_FORMAT_FACTORY.getInstance("shiro1");
    return format.format(hash);
  }

}
