package com.meltmedia.cadmium.core.api;

public class DeployRequest {
  private String branch;
  private String repo;
  private String domain;
  private String contextRoot;
  private String artifact;
  private boolean disableSecurity;
  
  public DeployRequest() {}

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getRepo() {
    return repo;
  }

  public void setRepo(String repo) {
    this.repo = repo;
  }

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

  public String getArtifact() {
    return artifact;
  }

  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  public boolean isDisableSecurity() {
    return disableSecurity;
  }

  public void setDisableSecurity(boolean disableSecurity) {
    this.disableSecurity = disableSecurity;
  }
}
