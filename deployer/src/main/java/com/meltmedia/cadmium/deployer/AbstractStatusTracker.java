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
package com.meltmedia.cadmium.deployer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.meltmedia.cadmium.core.messaging.ChannelMember;

public class AbstractStatusTracker implements StatusTracker {
  
  protected Map<String, List<String>> memberLogs = new Hashtable<String, List<String>>();
  protected Map<String, Boolean> memberStatus = new Hashtable<String, Boolean>();
  protected String domain;
  protected String contextRoot;
  
  /**
   * Creates a new instance with the required fields.
   * @param domain 
   * @param contextRoot
   */
  public AbstractStatusTracker(String domain, String contextRoot) {
    this.domain = domain;
    this.contextRoot = contextRoot;
  }
  
  public Map<String, List<String>> getMemberLogs() {
    return memberLogs;
  }

  public Map<String, Boolean> getMemberStatus() {
    return memberStatus;
  }

  public String getDomain() {
    return domain;
  }

  public String getContextRoot() {
    return contextRoot;
  }

  /**
   * @param member
   * @return The List of current deployment log messages for the given member.
   * Adds a new list and returns that in the case that the member has no list yet.
   */
  public List<String> getMemberLogs(ChannelMember member) {
    synchronized(this) {
      String key= getKey(member);
      if(!memberLogs.containsKey(key)) {
        memberLogs.put(key, new ArrayList<String>());
      }
      return memberLogs.get(key);
    }
  }
  
  /**
   * Adds a message to a members logs if the last log was not the same log.
   * 
   * @param member
   * @param message
   */
  public void logToMember(ChannelMember member, String message) {
    synchronized(this) {
      String key= getKey(member);
      if(!memberLogs.containsKey(key)) {
        memberLogs.put(key, new ArrayList<String>());
      }
      List<String> logs = memberLogs.get(key);
      if(logs.isEmpty() || logs.indexOf(message) != logs.size() - 1){
        logs.add(message);
      }
    }
  }
  
  /**
   * 
   * @param member
   * @return The current deployment status of the given member. Adds a false for the 
   * given member and returns that in the case that the member has no status yet.
   */
  public Boolean getMemberFinished(ChannelMember member) {
    synchronized(this) {
      String key= getKey(member);
      if(memberStatus.containsKey(key)) {
        return memberStatus.get(key);
      }
      return false;
    }
  }
  
  /**
   * Sets the member to finished.
   * 
   * @param member
   */
  public void setMemberFinished(ChannelMember member) {
    synchronized(this) {
      String key= getKey(member);
      memberStatus.put(key, true);
    }
  }
  
  /**
   * Sets the member to not finished.
   * 
   * @param member
   */
  public void setMemberStarted(ChannelMember member) {
    synchronized(this) {
      String key= getKey(member);
      memberStatus.put(key, false);
    }
  }

  /**
   * @return True if all members are finished.
   */
  public boolean isFinished() {
    for(String mem : memberStatus.keySet()) {
      Boolean memFinished = memberStatus.get(mem);
      if(!memFinished) {
        return false;
      }
    }
    return !memberStatus.isEmpty();
  }
  
  /**
   * Clears previously set values for all fields except the domain and contextRoot.
   */
  public void clear() {
    synchronized(this) {
      memberLogs.clear();
      memberStatus.clear();
    }
  }
  
  /**
   * Creates a String key from a {@link ChannelMember}.
   * @param member
   * @return
   */
  protected String getKey(ChannelMember member) {
    return member.toString();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((contextRoot == null) ? 0 : contextRoot.hashCode());
    result = prime * result + ((domain == null) ? 0 : domain.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractStatusTracker other = (AbstractStatusTracker) obj;
    if (contextRoot == null) {
      if (other.contextRoot != null)
        return false;
    } else if (!contextRoot.equals(other.contextRoot))
      return false;
    if (domain == null) {
      if (other.domain != null)
        return false;
    } else if (!domain.equals(other.domain))
      return false;
    return true;
  }

}
