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

import org.apache.http.HttpMessage;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Implements functionality to facilitate authentication to Cadmium rest endpoints.
 * 
 * @author John McEntire
 * @author Christian Trimble
 *
 */
public class AbstractAuthorizedOnly implements AuthorizedOnly {
  private static final Logger logger = LoggerFactory.getLogger(AbstractAuthorizedOnly.class);
  
  private static final Pattern URL_PATTERN = compilePattern("\\Ahttp://([^/:]+)(?:\\:\\d+)?(?:/[^/]*)*\\Z", logger);

  /**
   * Compiles the given regex.  Returns null if the pattern could not be compiled and logs an error message to the
   * specified logger.
   *
   * @param regex the regular expression to compile.
   * @param logger the logger to notify of errors.
   * @return The compiled regular expression, or null if it could not be compiled.
   */
  private static Pattern compilePattern( String regex, Logger logger ) {
    try {
      return Pattern.compile(regex);
    }
    catch( PatternSyntaxException pse ) {
      logger.error("Could not compile regex "+regex, pse);
    }
    return null;
  }

  /**
   * The Github API token for the user of the current instance.
   */
  protected String token;
  
  /**
   * @param token The users Github API token.
   */
  @Override
  public void setToken(String token) {
    this.token = token;
  }
  
  /**
   * @return The users Github API token.
   */
  @Override
  public String getToken() {
    return token;
  }
  
  /**
   * Adds the Authorization Header to the given message.
   * @param token The users Github API Token.
   * @param message The Apache Commons HttpClient message to add the authentication to.
   */
  protected static void addAuthHeader(String token, HttpMessage message) {
    message.addHeader("Authorization", "token " + token);
  }
  
  /**
   * Calls the static method with the same name to add the Authorization Header to the given message.
   * 
   * @param message The Apache Commons HttpClient message to add the authentication to.
   */
  protected void addAuthHeader(HttpMessage message) {
    addAuthHeader(token, message);
  }
  
  /**
   * Converts the passed in siteUrl to be https if not already or not local.
   * 
   * @param siteUrl The url of a cadmium site.
   * @return The passed in siteUrl or the same url but converted to https.
   */
  protected String getSecureBaseUrl(String siteUrl) {
    if(!siteUrl.startsWith("http://") && !siteUrl.startsWith("https://")) {
      siteUrl = "http://" + siteUrl;
    }
    Matcher urlMatcher = URL_PATTERN.matcher(siteUrl);
    if(urlMatcher.matches()) {
      logger.debug("Url matches [{}]", siteUrl);
      boolean local = false;
      try {
        logger.debug("Checking if host [{}] is local", urlMatcher.group(1));
        InetAddress hostAddr = InetAddress.getByName(urlMatcher.group(1));
        local = hostAddr.isLoopbackAddress() || hostAddr.isSiteLocalAddress();
        logger.debug("IpAddress [{}] local: {}", hostAddr.getHostAddress(), local);
      } catch(UnknownHostException e) {
        logger.warn("Hostname not found ["+siteUrl+"]", e);
      }
      if(!local) {
        return siteUrl.replaceFirst("http://", "https://");
      }
    }
    return siteUrl;
  }

  /**
   * Tells the {@link CadmiumCli} instance that this command should silence the authentication and just fail if not authorized.
   * 
   * @return true if the authentication should not prompt for a username and password.
   */
  @Override
  public boolean isAuthQuiet() {
    return false;
  }
  
  /**
   * Sets the Commons HttpComponents to accept all SSL Certificates.
   * 
   * @throws Exception
   * @return An instance of HttpClient that will accept all.
   */
  protected static HttpClient httpClient() throws Exception {
    return HttpClients.custom()
        .setHostnameVerifier(new AllowAllHostnameVerifier())
        .setSslcontext(SSLContexts.custom()
            .loadTrustMaterial(null, new TrustStrategy() {
              @Override
              public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
              }
            })
            .build())
        .build();
  }

}
