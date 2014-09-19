package com.meltmedia.cadmium.core.history.loggly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.meltmedia.cadmium.core.SharedContentRoot;
import com.meltmedia.cadmium.core.api.VHost;
import com.meltmedia.cadmium.core.config.ConfigManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * Simple Api to loggly that sends log events over https.
 *
 * @author jmcentire
 */
@Singleton
public class Api {
  public static final String INPUT_KEY_FILE_NAME = "loggly.key";
  public static final String LOGGLY_BASE_URL_OVERRIDE_KEY = "com.meltmedia.cadmium.loggly.base-url";
  public static final String LOGGLY_CUSTOMER_TOKEN_OVERRIDE_KEY = "com.meltmedia.cadmium.loggly.client-token";
  public static final String LOGGLY_BASE_URL = "https://logs-01.loggly.com/inputs/";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected String customerToken;

  @Inject
  @VHost
  protected String vHostName;

  @Inject
  protected ConfigManager configManager;

  protected String environment;

  private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy/MM/dd HH:mm:ss.SSS").create();

  @Inject
  public Api(@SharedContentRoot String sharedContentRoot) {
    readInLogglyKey(sharedContentRoot, INPUT_KEY_FILE_NAME);

    if(StringUtils.isEmpty(customerToken)) {
      logger.debug("Loggly is not configured. I will not log then.");
    } else {
      logger.debug("Loggly is configured {}.", customerToken);
    }
  }

  private void readInLogglyKey(String sharedContentRoot, String keyFileName) {
    File logglyKeyFile = new File(sharedContentRoot, keyFileName);
    if(logglyKeyFile.exists() && logglyKeyFile.canRead()) {
      try {
        customerToken = FileUtils.readFileToString(logglyKeyFile);
      } catch(Throwable t) {
        logger.warn("Failed to read in loggly input key from file. (" + keyFileName + ")", t);
      }
    }

    if(StringUtils.isEmpty(customerToken)) {
      InputStream in = null;
      try {
        in = getClass().getClassLoader().getResourceAsStream(keyFileName);
        if(in != null) {
          customerToken = IOUtils.toString(in);
        }
      } catch (Throwable t) {
        logger.warn("Failed to load loggly input key from classpath. (" + keyFileName + ")", t);
      } finally {
        if(in != null) {
          IOUtils.closeQuietly(in);
        }
      }
    }

    if(StringUtils.isNotBlank(customerToken)) {
      customerToken = customerToken.trim();
    }
  }

  private String getCustomerToken() {
    return configManager
        .getDefaultProperties()
        .getProperty(LOGGLY_CUSTOMER_TOKEN_OVERRIDE_KEY, customerToken);
  }

  private String getLogglyUrl() {
    return System.getProperty(LOGGLY_BASE_URL_OVERRIDE_KEY, LOGGLY_BASE_URL) + getCustomerToken();
  }

  public void sendEvent(Event evt) {
    if (StringUtils.isEmpty(environment)) {
      environment = configManager.getDefaultProperties().getProperty("com.meltmedia.cadmium.environment", "development");
    }
    evt.setEnvironment(environment);
    evt.setDomain(vHostName);
    logger.info(GSON.toJson(evt));
    if(!StringUtils.isEmpty(customerToken)) {
      logger.debug("Sending event {}", evt);

      HttpPost post = new HttpPost(getLogglyUrl() + "/tag/" + evt.getTag());

      post.setEntity(new StringEntity(GSON.toJson(evt), ContentType.TEXT_PLAIN));

      try {
        HttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
        HttpResponse response = client.execute(post);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          logger.debug("Event sent to loggly {}", evt);
          EntityUtils.consumeQuietly(response.getEntity());
        } else {
          String respStr = EntityUtils.toString(response.getEntity());
          throw new Exception("Unexpected result["+response.getStatusLine().getStatusCode()+"]: "+respStr);
        }
      } catch (Throwable t) {
        logger.debug("Failed to send loggly event.", t);
      } finally {
        post.releaseConnection();
      }
    }
  }

  protected static DefaultHttpClient setTrustAllSSLCerts(DefaultHttpClient client) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
    SSLSocketFactory acceptAll = new SSLSocketFactory(new TrustSelfSignedStrategy(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, acceptAll));

    return client;
  }
}
