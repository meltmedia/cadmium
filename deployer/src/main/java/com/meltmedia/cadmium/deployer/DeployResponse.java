package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.commands.AbstractMessageBean;

/**
 * Holds the response created war name or exception.
 */
public class DeployResponse extends AbstractMessageBean {
  private String warName;
  private Throwable error;

  public DeployResponse(){}

  public String getWarName() {
    return warName;
  }

  public void setWarName(String warName) {
    this.warName = warName;
  }

  public Throwable getError() {
    return error;
  }

  public void setError(Throwable error) {
    this.error = error;
  }
}
