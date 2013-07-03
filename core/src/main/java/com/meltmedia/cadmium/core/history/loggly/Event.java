package com.meltmedia.cadmium.core.history.loggly;

import com.meltmedia.cadmium.core.history.HistoryEntry;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * com.meltmedia.cadmium.core.history.loggly.Event
 *
 * @author jmcentire
 */
public class Event {

  private Date timestamp;
  private String branch;
  private String revision;
  private String repoUrl;
  private String userId;
  private String comment;
  private HistoryEntry.EntryType type;
  private boolean maintenance;
  private String environment;
  private String domain;

  public Event(HistoryEntry history) {
    timestamp = history.getTimestamp();
    branch = history.getBranch();
    revision = history.getRevision();
    repoUrl = history.getRepoUrl();
    userId = history.getOpenId();
    comment = history.getComment();
    type = history.getType();
    maintenance = history.isMaintenance();
  }

  public Event(){}

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
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

  public HistoryEntry.EntryType getType() {
    return type;
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

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
