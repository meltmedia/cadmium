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
package com.meltmedia.cadmium.core.commands;

import java.util.Date;

/**
 * Questions:
 *   1) The old code uses both the key 'CurrentRevision' and 'sha'.  What is the difference?
 *   
 * @author Christian Trimble
 *
 */
public class ContentUpdateRequest extends AbstractMessageBody {
  
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
