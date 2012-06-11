package com.meltmedia.cadmium.cli;

import org.apache.http.HttpMessage;

public class AbstractAuthorizedOnly implements AuthorizedOnly {

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

}
