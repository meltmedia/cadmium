package com.meltmedia.cadmium.jetty;

import java.util.Map;

/**
 * com.meltmedia.cadmium.jetty.MailSessionJNDI
 *
 * @author jmcentire
 */
public class MailSessionJNDI {
  private String jndiName;
  private String username;
  private String password;
  private Map<String, String> properties;

  public String getJndiName() {
    return jndiName;
  }

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
