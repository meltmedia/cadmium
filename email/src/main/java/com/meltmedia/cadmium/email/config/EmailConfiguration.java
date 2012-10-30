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
  private String defaultFromAddress;
  private String jndiName;
  private String messageTransformer;
  private String sessionStrategy;
  

	public EmailConfiguration(){}
	
	public String getDefaultFromAddress() {
		return defaultFromAddress;
	}
	
	public String getJndiName() {
    return jndiName;
  }

  public String getMessageTransformer() {
    return messageTransformer;
  }

  public String getSessionStrategy() {
    return sessionStrategy;
  }

  public void setDefaultFromAddress(String defaultFromAddress) {
		this.defaultFromAddress = defaultFromAddress;
	}

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  public void setMessageTransformer(String messageTransformer) {
    this.messageTransformer = messageTransformer;
  }

  public void setSessionStrategy(String sessionStrategy) {
    this.sessionStrategy = sessionStrategy;
  }
}
