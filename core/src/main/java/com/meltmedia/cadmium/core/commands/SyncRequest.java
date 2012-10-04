package com.meltmedia.cadmium.core.commands;

public class SyncRequest extends AbstractMessageBody {
  protected String repo;
  protected String branch;
  protected String sha;
  protected String configRepo;
  protected String configBranch;
  protected String configSha;
  private String comment;
  public String getRepo() {
    return repo;
  }
  public void setRepo(String repo) {
    this.repo = repo;
  }
  public String getBranch() {
    return branch;
  }
  public void setBranch(String branch) {
    this.branch = branch;
  }
  public String getSha() {
    return sha;
  }
  public void setSha(String sha) {
    this.sha = sha;
  }
  public String getConfigRepo() {
    return configRepo;
  }
  public void setConfigRepo(String configRepo) {
    this.configRepo = configRepo;
  }
  public String getConfigBranch() {
    return configBranch;
  }
  public void setConfigBranch(String configBranch) {
    this.configBranch = configBranch;
  }
  public String getConfigSha() {
    return configSha;
  }
  public void setConfigSha(String configSha) {
    this.configSha = configSha;
  }
  public void setComment(String comment) {
    this.comment = comment;
  }
  public String getComment() {
    return comment;
  }
}
