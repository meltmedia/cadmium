package com.meltmedia.cadmium.core.util;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class CadmiumHttpClientBuilder {

  public static HttpClient getCadmiumHttpClient() throws KeyManagementException, NoSuchAlgorithmException {
    SSLContext sslContext = SSLContexts.custom()
        .useTLS()
        .build();

    SSLConnectionSocketFactory f = new SSLConnectionSocketFactory(
        sslContext,
        new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"},
        null,
        new org.apache.http.conn.ssl.AllowAllHostnameVerifier());

    return HttpClients.custom()
        .setSSLSocketFactory(f)
        .build();
  }
}
