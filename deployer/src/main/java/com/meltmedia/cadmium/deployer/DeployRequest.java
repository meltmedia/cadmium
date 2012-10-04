package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.commands.AbstractMessageBody;

public class DeployRequest extends AbstractMessageBody {
  protected String domain;
  protected String branch;
  protected String repo;
  protected String configBranch;
  protected String configRepo;
  protected String context;
  protected String artifact;
  private boolean secure;
  public String getDomain() {
    return domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }
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
  public String getConfigBranch() {
    return configBranch;
  }
  public void setConfigBranch(String configBranch) {
    this.configBranch = configBranch;
  }
  public String getConfigRepo() {
    return configRepo;
  }
  public void setConfigRepo(String configRepo) {
    this.configRepo = configRepo;
  }
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }
  public String getArtifact() {
    return artifact;
  }
  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }
  public boolean getSecure() {
    return secure;
  }
  public void setSecure(boolean secure) {
    this.secure = secure;
  }
}
