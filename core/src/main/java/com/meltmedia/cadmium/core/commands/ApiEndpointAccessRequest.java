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
