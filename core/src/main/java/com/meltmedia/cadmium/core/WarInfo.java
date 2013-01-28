package com.meltmedia.cadmium.core;

import java.util.ArrayList;
import java.util.List;

public class WarInfo {
  private String warName;
  private String domain;
  private String context;
  private String repo;
  private String configRepo;
  private String contentBranch;
  private String configBranch;
  private List<MavenVector> artifacts = new ArrayList<MavenVector>();
  
  public WarInfo(){}

  public String getWarName() {
    return warName;
  }

  public void setWarName(String warName) {
    this.warName = warName;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getRepo() {
    return repo;
  }

  public void setRepo(String repo) {
    this.repo = repo;
  }

  public String getConfigRepo() {
    return configRepo;
  }

  public void setConfigRepo(String configRepo) {
    this.configRepo = configRepo;
  }

  public String getContentBranch() {
    return contentBranch;
  }

  public void setContentBranch(String contentBranch) {
    this.contentBranch = contentBranch;
  }

  public String getConfigBranch() {
    return configBranch;
  }

  public void setConfigBranch(String configBranch) {
    this.configBranch = configBranch;
  }

  public List<MavenVector> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(List<MavenVector> artifacts) {
    this.artifacts = artifacts;
  }
}
