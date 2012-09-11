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
package com.meltmedia.cadmium.email.config;

import com.meltmedia.cadmium.core.config.CadmiumConfig;

@CadmiumConfig(EmailConfiguration.KEY)
public class EmailConfiguration {
  public static final String KEY = "Email";
  private String jndiName;
  private String sessionStrategy;
  private String messageTransformer;
  
  public EmailConfiguration(){}

  public String getJndiName() {
    return jndiName;
  }

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  public String getSessionStrategy() {
    return sessionStrategy;
  }

  public void setSessionStrategy(String sessionStrategy) {
    this.sessionStrategy = sessionStrategy;
  }

  public String getMessageTransformer() {
    return messageTransformer;
  }

  public void setMessageTransformer(String messageTransformer) {
    this.messageTransformer = messageTransformer;
  }
}
