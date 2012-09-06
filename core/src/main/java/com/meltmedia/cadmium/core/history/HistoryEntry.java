/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.history;

import java.util.Date;

public class HistoryEntry {
  public static enum EntryType { CONTENT, CONFIG, MAINT }
  
  private long index = 1;
  private Date timestamp;
  private String branch;
  private String revision;
  private String repoUrl;
  private long timeLive;
  private String openId;
  private String servedDirectory;
  private boolean revertible;
  private boolean maintenance;
  private boolean failed = false;
  private boolean finished = true;
  private String uuid;
  private String comment;
  private EntryType type;
  
  public HistoryEntry(){}
  
  public HistoryEntry(EntryType type, Date timestamp, String repoUrl, String branch, String revision, long timeLive, String openId, String servedDirectory, boolean revertible, String comment) {
    this.type = type;
    this.timestamp = timestamp;
    this.repoUrl = repoUrl;
    this.branch = branch;
    this.revision = revision;
    this.timeLive = timeLive;
    this.openId = openId;
    this.servedDirectory = servedDirectory;
    this.revertible = revertible;
    this.comment = comment;
  }
  
  public long getIndex() {
    return index;
  }
  
  public void setIndex(long index) {
    this.index = index;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getRepoUrl() {
    return repoUrl;
  }

  public void setRepoUrl(String repoUrl) {
    this.repoUrl = repoUrl;
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

  public long getTimeLive() {
    return timeLive;
  }

  public void setTimeLive(long timeLive) {
    this.timeLive = timeLive;
  }

  public String getOpenId() {
    return openId;
  }

  public void setOpenId(String openId) {
    this.openId = openId;
  }

  public String getServedDirectory() {
    return servedDirectory;
  }

  public void setServedDirectory(String servedDirectory) {
    this.servedDirectory = servedDirectory;
  }

  public boolean isRevertible() {
    return revertible;
  }

  public void setRevertible(boolean revertible) {
    this.revertible = revertible;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public void setMaintenance(boolean maint) {
    this.maintenance = maint;
  }
  
  public boolean isMaintenance() {
    return this.maintenance;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public boolean isFailed() {
    return failed;
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }

  public EntryType getType() {
    return type;
  }

  public void setType(EntryType type) {
    this.type = type;
  }
}
