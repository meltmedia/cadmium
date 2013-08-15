package com.meltmedia.cadmium.deployer.jboss7;

import com.meltmedia.cadmium.core.config.CadmiumConfig;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * com.meltmedia.cadmium.deployer.jboss7.JBossManagementConfiguration
 *
 * @author jmcentire
 */
@CadmiumConfig(JBossManagementConfiguration.KEY)
public class JBossManagementConfiguration {
  public static final String KEY = "jboss-api";

  private String username;
  private String password;
  private String host = "localhost";
  private Integer port = 9990;

  public JBossManagementConfiguration(){}

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

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
