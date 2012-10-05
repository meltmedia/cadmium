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
