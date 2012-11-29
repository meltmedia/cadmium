package com.meltmedia.cadmium.core.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;


/**
 * Utility class that gets this machines external facing ip address.
 * 
 * This utility uses 'http://automation.whatismyip.com/n09230945.asp' to get the ip address.
 * 
 * @author John McEntire
 *
 */
public final class PublicIpUtils {
  
  private PublicIpUtils(){}
  
  /**
   * Looks up the external ip address of the machine that this vm is running on.
   * 
   * @return
   * @throws Exception
   */
  public static String lookup() throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    client.getParams().setParameter(
        CoreProtocolPNames.USER_AGENT, 
        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0");
    HttpGet get = new HttpGet("http://automation.whatismyip.com/n09230945.asp");
    
    HttpResponse response = null;
    String ipAddress = null;
    try {
      response = client.execute(get);
      if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        ipAddress = EntityUtils.toString(response.getEntity());
      } else {
        EntityUtils.consume(response.getEntity());
      }
    } finally {
      get.releaseConnection();
    }
    return ipAddress;
  }

}