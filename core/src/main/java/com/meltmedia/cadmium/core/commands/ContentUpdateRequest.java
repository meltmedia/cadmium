package com.meltmedia.cadmium.core.commands;

import java.util.Date;

/**
 * Questions:
 *   1) The old code uses both the key 'CurrentRevision' and 'sha'.  What is the difference?
 *   
 * @author Christian Trimble
 *
 */
public class ContentUpdateRequest {
  
  protected String repo;
  protected String branchName;
  protected String sha;
  protected String currentRevision;
  protected String openId;
  protected Date lastUpdated;
  protected String uuid;
  protected String comment;
  protected boolean revertable;
  
  public String getRepo() {
    return repo;
  }
  public void setRepo(String repo) {
    this.repo = repo;
  }
  public String getBranchName() {
    return branchName;
  }
  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }
  public String getCurrentRevision() {
    return currentRevision;
  }
  public void setCurrentRevision(String currentRevision) {
    this.currentRevision = currentRevision;
  }
  public String getOpenId() {
    return openId;
  }
  public void setOpenId(String openId) {
    this.openId = openId;
  }
  public Date getLastUpdated() {
    return lastUpdated;
  }
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
  public String getComment() {
    return comment;
  }
  public void setComment(String comment) {
    this.comment = comment;
  }
  public boolean isRevertable() {
    return revertable;
  }
  public void setRevertable(boolean revertable) {
    this.revertable = revertable;
  }
  public String getSha() {
    return sha;
  }
  public void setSha(String sha) {
    this.sha = sha;
  }


}
