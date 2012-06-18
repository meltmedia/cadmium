package com.meltmedia.cadmium.vault.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.vault.SafetyMissingException;

@Singleton
public class ResourceFetcher {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final SimpleDateFormat headerFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
  private static final String urlPathInfo = "public/resource";

  private final String vaultBaseUrl;
  private final HttpClient httpClient;
  
  @Inject
  public ResourceFetcher(@Named(VaultConstants.BASE_URL) String vaultBaseUrl) {
    this.vaultBaseUrl = vaultBaseUrl;
    httpClient = new DefaultHttpClient();
  }

  public String fetch(String resourceGuid, Date lastModified) throws SafetyMissingException, IOException {
    log.debug("Getting new resource from vault: {}:{}", resourceGuid, lastModified);
    HttpGet get = new HttpGet((vaultBaseUrl + "/" + urlPathInfo + "/" + resourceGuid).replaceAll("[/]{2,}", "/").replace(":/", "://"));
    if(lastModified != null) {
      get.addHeader("If-Modified-Since", headerFormat.format(lastModified));
    }
    HttpResponse httpResponse = null;
    try {
      httpResponse = httpClient.execute(get);
      if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        log.debug("Resource[{}] not modified", resourceGuid);
        return null;
      } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        String safety = null;
        try {
          safety = EntityUtils.toString(httpResponse.getEntity());
          log.debug("Retrieved new safety [{}]", safety);
          if(safety == null || safety.trim().length() == 0) {
            throw new SafetyMissingException("Vault responded with no content.");
          }
        } catch(Exception e) {
          throw new SafetyMissingException("An error happened while reading vault response.");
        }
        return safety;
      } else {
        log.debug("Bad response from vault: {}", httpResponse.getStatusLine().toString());
        throw new IOException("Vault["+get.getURI()+"] responded with: "+httpResponse.getStatusLine());
      }
    } finally {
      get.releaseConnection();
    }
  }

}
