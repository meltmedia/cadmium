package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.commands.AbstractMessageBean;

/**
 * Response for deployments checks.
 */
public class DeploymentCheckResponse extends AbstractMessageBean {
  private boolean finished;
  private boolean started;
  private Throwable error;

  public DeploymentCheckResponse(){}

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }

  public Throwable getError() {
    return error;
  }

  public void setError(Throwable error) {
    this.error = error;
  }

  public boolean isStarted() {
    return started;
  }

  public void setStarted(boolean started) {
    this.started = started;
  }
}
