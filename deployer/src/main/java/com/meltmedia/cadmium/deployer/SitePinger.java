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
package com.meltmedia.cadmium.deployer;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will send a http request to localhost to the given domain and contextRoot.
 * 
 * @author John McEntire
 *
 */
public class SitePinger implements Closeable {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  /**
   * The possible statuses returned from a ping request.
   * 
   * @author John McEntire
   *
   */
  public static enum PingStatus {UNAVAILABLE, IN_MAINTENANCE, OK};
  
  private URI url;
  private String domain;
  private HttpClient client;
  
  /**
   * Sets up a new instance of this class to be able to send http requests
   * to a Cadmium war's <code>/system/status/Ping</code> rest endpoint.
   * 
   * @param domain The domain to send to localhost.
   * @param contextRoot The context root of the deployed Cadmium war.
   * @throws Exception Thrown if the domain and contextRoot combination makes an invalid URI.
   */
  public SitePinger(String domain, String contextRoot) throws Exception {
    this.domain = domain;
    String pingUrl = domain + "/" + contextRoot + "/system/status/Ping";
    pingUrl = "http://" + pingUrl.replaceAll("/+", "/");
    url = new URI(pingUrl);
    
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", 80, new PlainSocketFactory()));
    
    PoolingClientConnectionManager connMng = new PoolingClientConnectionManager(schemeRegistry, 10, TimeUnit.SECONDS, new LoopbackDnsResolver());
    connMng.setDefaultMaxPerRoute(1);
    connMng.setMaxTotal(1);
    
    client = new DefaultHttpClient(connMng);
    
    client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
    client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 1000);
  }
  
  /**
   * This will send the actual http ping request.
   * 
   * @return The response status of the request.
   * @throws CadmiumDeploymentException when requested site responds with a 500.
   */
  public PingStatus ping() throws CadmiumDeploymentException {
    PingStatus status = PingStatus.UNAVAILABLE;
    HttpGet get = null;
    try {
      get = new HttpGet(url);
      
      HttpResponse response = client.execute(get);
      
      int statusCode = response.getStatusLine().getStatusCode();
      if(statusCode == HttpStatus.SC_OK) {
        status = PingStatus.OK;
      } else if(statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
        status = PingStatus.IN_MAINTENANCE;
      } else if(statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
        String responseStr = EntityUtils.toString(response.getEntity());
        throw new CadmiumDeploymentException(responseStr);
      }
    } catch(IOException e) {
      log.info("Failed to get response from "+url, e);
    } finally {
      if(get != null) {
        get.releaseConnection();
      }
    }
    
    return status;
  }
  
  
  
  /**
   * A DnsResolver that will resolve the domain associated with the 
   * site that is going to be pinged to localhost.
   * 
   * @author John McEntire
   *
   */
  private class LoopbackDnsResolver extends SystemDefaultDnsResolver {

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
      if(host.equalsIgnoreCase(domain)) {
        return new InetAddress[] {InetAddress.getLocalHost()};
      }
      return super.resolve(host);
    }
    
  }



  @Override
  public void close() throws IOException {
    client.getConnectionManager().shutdown();
  }
}
