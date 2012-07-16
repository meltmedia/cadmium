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
package com.meltmedia.cadmium.cli;

import org.apache.http.HttpMessage;

public class AbstractAuthorizedOnly implements AuthorizedOnly {

  protected String token;
  
  @Override
  public void setToken(String token) {
    this.token = token;
  }
  
  @Override
  public String getToken() {
    return token;
  }
  
  protected static void addAuthHeader(String token, HttpMessage message) {
    message.addHeader("Authorization", "token " + token);
  }
  
  protected void addAuthHeader(HttpMessage message) {
    addAuthHeader(token, message);
  }

}
