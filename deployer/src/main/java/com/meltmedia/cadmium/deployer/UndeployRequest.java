package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.commands.AbstractMessageBody;

public class UndeployRequest extends AbstractMessageBody {

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
