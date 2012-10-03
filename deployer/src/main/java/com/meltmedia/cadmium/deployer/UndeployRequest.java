package com.meltmedia.cadmium.deployer;

public class UndeployRequest {

  private String domain;
  private String context;

  public String getDomain() {
    return domain;
  }

  public String getContext() {
    return context;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setContext(String context) {
    this.context = context;
  }

}
