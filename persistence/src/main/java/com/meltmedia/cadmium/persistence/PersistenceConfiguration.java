package com.meltmedia.cadmium.persistence;

import java.util.HashMap;
import java.util.Map;

import com.meltmedia.cadmium.core.config.CadmiumConfig;

/**
 * Configuration class for the configuration of the database persistence.
 * 
 * @author John McEntire
 *
 */
@CadmiumConfig(PersistenceConfiguration.CONFIGURATION_KEY)
public class PersistenceConfiguration {
  public static final String CONFIGURATION_KEY = "jpa";
  private String jndiName;
  private String databaseDialect;
  private Map<String, String> extraConfiguration = new HashMap<String, String>();
  
  public PersistenceConfiguration() {}

  public String getJndiName() {
    return jndiName;
  }

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  public String getDatabaseDialect() {
    return databaseDialect;
  }

  public void setDatabaseDialect(String databaseDialect) {
    this.databaseDialect = databaseDialect;
  }

  public Map<String, String> getExtraConfiguration() {
    return extraConfiguration;
  }

  public void setExtraConfiguration(Map<String, String> extraConfiguration) {
    this.extraConfiguration = extraConfiguration;
  }
}
