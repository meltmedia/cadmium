package com.meltmedia.cadmium.jetty;

/**
 * com.meltmedia.cadmium.jetty.DataSourceJNDI
 *
 * @author jmcentire
 */
public class DataSourceJNDI {
  private String jndiName;
  private String connectionUrl;
  private String driverClass;
  private String username;
  private String password;
  private Integer initSize;
  private Integer minPoolSize;
  private Integer maxPoolSize;
  private String testSQL;

  public String getJndiName() {
    return jndiName;
  }

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  public String getConnectionUrl() {
    return connectionUrl;
  }

  public void setConnectionUrl(String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }

  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
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

  public Integer getInitSize() {
    return initSize;
  }

  public void setInitSize(Integer initSize) {
    this.initSize = initSize;
  }

  public Integer getMinPoolSize() {
    return minPoolSize;
  }

  public void setMinPoolSize(Integer minPoolSize) {
    this.minPoolSize = minPoolSize;
  }

  public Integer getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(Integer maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  public String getTestSQL() {
    return testSQL;
  }

  public void setTestSQL(String testSQL) {
    this.testSQL = testSQL;
  }
}
