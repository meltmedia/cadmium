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
