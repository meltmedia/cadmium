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
  public static final String LOGGLY_BASE_URL = "https://logs.loggly.com/inputs/";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected String inputKey;

  @Inject
  @VHost
  protected String vHostName;

  @Inject
  protected ConfigManager configManager;

  protected String environment;

  private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy/MM/dd HH:mm:ss.SSS").create();

  @Inject
  public Api(@SharedContentRoot String sharedContentRoot) {
    File logglyKeyFile = new File(sharedContentRoot, INPUT_KEY_FILE_NAME);
    if(logglyKeyFile.exists() && logglyKeyFile.canRead()) {
      try {
        inputKey = FileUtils.readFileToString(logglyKeyFile);
      } catch(Throwable t) {
        logger.warn("Failed to read in loggly input key from file. (" + INPUT_KEY_FILE_NAME + ")", t);
      }
    }

    if(StringUtils.isEmpty(inputKey)) {
      InputStream in = null;
      try {
        in = getClass().getClassLoader().getResourceAsStream(INPUT_KEY_FILE_NAME);
        if(in != null) {
          inputKey = IOUtils.toString(in);
        }
      } catch (Throwable t) {
        logger.warn("Failed to load loggly input key from classpath. (" + INPUT_KEY_FILE_NAME + ")", t);
      } finally {
        if(in != null) {
          IOUtils.closeQuietly(in);
        }
      }
    }

    if(StringUtils.isEmpty(inputKey)) {
      logger.info("Loggly is not configured. I will not log then.");
    } else {
      logger.info("Loggly is configured {}.", inputKey);
    }
  }

  public void sendEvent(Event evt) {
    if(!StringUtils.isEmpty(inputKey)) {
      logger.debug("Sending event {}", evt);
      if (StringUtils.isEmpty(environment)) {
        environment = configManager.getDefaultProperties().getProperty("com.meltmedia.cadmium.environment", "development");
      }
      evt.setEnvironment(environment);
      evt.setDomain(vHostName);

      HttpPost post = new HttpPost(LOGGLY_BASE_URL + inputKey.trim());

      post.setEntity(new StringEntity(GSON.toJson(evt), ContentType.APPLICATION_JSON));

      try {
        HttpClient client = setTrustAllSSLCerts(new DefaultHttpClient());
        HttpResponse response = client.execute(post);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          logger.info("Event sent to loggly {}", evt);
          EntityUtils.consumeQuietly(response.getEntity());
        } else {
          String respStr = EntityUtils.toString(response.getEntity());
          throw new Exception("Unexpected result["+response.getStatusLine().getStatusCode()+"]: "+respStr);
        }
      } catch (Throwable t) {
        logger.error("Failed to send loggly event.", t);
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
