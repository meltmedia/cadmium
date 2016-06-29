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
 *   
 * @author Christian Trimble
 *
 */
public class ContentUpdateRequest extends AbstractMessageBean {
  
  protected GitLocation contentLocation;
  protected String openId;
  protected Date lastUpdated;
  protected String uuid;
  protected String comment;
  protected boolean revertable;
  protected String failureReason;
  
  public GitLocation getContentLocation() {
    return contentLocation;
  }
  public void setContentLocation(GitLocation contentLocation) {
    this.contentLocation = contentLocation;
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
  public String getFailureReason() {
    return failureReason;
  }
  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }
}
