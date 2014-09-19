package com.meltmedia.cadmium.core.history.loggly;

import com.meltmedia.cadmium.core.history.HistoryEntry;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * com.meltmedia.cadmium.core.history.loggly.Event
 *
 * @author jmcentire
 */
public class HistoryEvent extends Event {

  private String branch;
  private String revision;
  private String repoUrl;
  private String userId;
  private String comment;
  private HistoryEntry.EntryType type;
  private boolean maintenance;

  public HistoryEvent(HistoryEntry history) {
    super();
    timestamp = history.getTimestamp();
    branch = history.getBranch();
    revision = history.getRevision();
    repoUrl = history.getRepoUrl();
    userId = history.getOpenId();
    comment = history.getComment();
    type = history.getType();
    maintenance = history.isMaintenance();
  }

  public HistoryEvent(){
    super();
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public String getRepoUrl() {
    return repoUrl;
  }

  public void setRepoUrl(String repoUrl) {
    this.repoUrl = repoUrl;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getType() {
    return type.toString();
  }

  public void setType(HistoryEntry.EntryType type) {
    this.type = type;
  }

  public boolean isMaintenance() {
    return maintenance;
  }

  public void setMaintenance(boolean maintenance) {
    this.maintenance = maintenance;
  }

  public String getTag() {
    return getDomain();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
