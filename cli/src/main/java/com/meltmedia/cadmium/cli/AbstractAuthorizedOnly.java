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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpMessage;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractAuthorizedOnly implements AuthorizedOnly {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private static final Pattern URL_PATTERN = Pattern.compile("\\Ahttp://([^/:]+)(?:\\:\\d+)?(?:/[^/]*)*\\Z");

  protected String token;
  
  @Override
  public void setToken(String token) {
    this.token = token;
  }
  
  @Override
  public String getToken() {
    return token;
  }
  
  protected static void addAuthHeader(String token, HttpMessage message) {
    message.addHeader("Authorization", "token " + token);
  }
  
  protected void addAuthHeader(HttpMessage message) {
    addAuthHeader(token, message);
  }
  
  protected String getSecureBaseUrl(String siteUrl) {
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

  @Override
  public boolean isAuthQuiet() {
    return false;
  }
  
  /**
   * Sets the Commons HttpComponents to accept all SSL Certificates.
   * 
   * @throws Exception
   * @return The reference passed in.
   * @throws KeyStoreException 
   * @throws NoSuchAlgorithmException 
   * @throws UnrecoverableKeyException 
   * @throws KeyManagementException 
   */
  protected static DefaultHttpClient setTrustAllSSLCerts(DefaultHttpClient client) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
    SSLSocketFactory acceptAll = new SSLSocketFactory(new TrustSelfSignedStrategy(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    
    client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, acceptAll));
    
    return client;
  }

}
