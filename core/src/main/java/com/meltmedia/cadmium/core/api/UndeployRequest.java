package com.meltmedia.cadmium.core.api;

public class UndeployRequest {
  private String domain;
  private String contextRoot;
  
  public UndeployRequest() {}

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getContextRoot() {
    return contextRoot;
  }

  public void setContextRoot(String contextRoot) {
    this.contextRoot = contextRoot;
  }
}
