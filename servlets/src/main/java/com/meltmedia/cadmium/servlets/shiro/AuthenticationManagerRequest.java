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
package com.meltmedia.cadmium.servlets.shiro;

import com.meltmedia.cadmium.core.commands.AbstractMessageBean;

/**
 * Request message for JGroups communication of an authentication update for this sites cluster only.
 * 
 * @author John McEntire
 *
 */
public class AuthenticationManagerRequest extends AbstractMessageBean {
  /**
   * An enum to specify the type of a request to update authentication.
   * 
   * @author John McEntire
   *
   */
  public static enum RequestType {
    /**
     * <p>Used to specify that the request is intended to add a user account.</p>
     * <p>When this is specified both the accountName and password fields are both required.</p>
     */
    ADD,
    
    /**
     * <p>Used to specify that the request is intended to remove a user account.</p>
     * <p>When this is specified the only required field is the accountName.</p>
     */
    REMOVE 
  };
  
  private RequestType requestType;
  private String accountName;
  private String password;
  
  public RequestType getRequestType() {
    return requestType;
  }
  
  public void setRequestType(RequestType requestType) {
    this.requestType = requestType;
  }
  
  public String getAccountName() {
    return accountName;
  }
  
  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
}
