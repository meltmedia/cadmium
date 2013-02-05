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
