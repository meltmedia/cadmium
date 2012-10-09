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
