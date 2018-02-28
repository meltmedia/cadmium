package com.meltmedia.cadmium.core.util;

import org.apache.http.client.HttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class CadmiumHttpClientBuilderTest {


  @Test
  public void testCadmiumHttpClientBuilder() throws NoSuchAlgorithmException, KeyManagementException {
    HttpClient httpClient = CadmiumHttpClientBuilder.getCadmiumHttpClient();
    Assert.assertNotNull(httpClient);
  }
}
