package com.meltmedia.cadmium.core.commands;

/**
 * Request used to control access to open api endpoints.
 * 
 * @author John McEntire
 *
 */
public class ApiEndpointAccessRequest extends AbstractMessageBean {
  public static enum UpdateOpteration {DISABLE, ENABLE};
  
  private UpdateOpteration operation;
  private String[] endpoints;
  
  public UpdateOpteration getOperation() {
    return operation;
  }
  public void setOperation(UpdateOpteration operation) {
    this.operation = operation;
  }
  public String[] getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(String[] endpoints) {
    this.endpoints = endpoints;
  }
}
