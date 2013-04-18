package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.commands.AbstractMessageBean;

/**
 * Request to check if a deployment has completed or failed.
 */
public class DeploymentCheckRequest extends AbstractMessageBean {
  public String warName;

  public DeploymentCheckRequest(){}

  public String getWarName() {
    return warName;
  }

  public void setWarName(String warName) {
    this.warName = warName;
  }
}
