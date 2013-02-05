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
package com.meltmedia.cadmium.servlets.shiro;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.realm.text.PropertiesRealm;

/**
 * A Shiro PropertiesRealm that allows runtime programatic acount information updates. 
 * 
 * @author John McEntire
 *
 */
public class PersistablePropertiesRealm extends PropertiesRealm {
  public static final String REALM_FILE_NAME = "security-realm.properties";
  private static final String USERNAME_PREFIX = "user.";
  
  public PersistablePropertiesRealm() {
    super();
    this.setName("cadmium-realm");
    this.setCredentialsMatcher(new PasswordMatcher());
  }

  /**
   * Disable reload thread!!!
   */
  @Override
  protected void startReloadThread() {}
  
  /**
   * Dump properties file backed with this Realms Users and roles.
   * 
   * @return
   */
  public Properties getProperties() {
    Properties props = new Properties();
    for(String name : this.users.keySet()) {
      SimpleAccount acct = this.users.get(name);
      props.setProperty(USERNAME_PREFIX + name, acct.getCredentials().toString());
    }
    return props;
  }
  
  /**
   * Removes an existing account.
   * @param username
   */
  public void removeUser(String username) {
    if(this.accountExists(username)) {
      this.users.remove(username);
    }
  }
  
  /**
   * Lists all existing user accounts.
   * @return
   */
  public List<String> listUsers() {
    List<String> users = new ArrayList<String>(this.users.keySet());
    Collections.sort(users);
    return users;
  }
  
  /**
   * loads a properties file named {@link REALM_FILE_NAME} in the directory passed in.
   */
  public void loadProperties(File contentDir)  {
    String resourceFile = null;
    if(contentDir.exists() && contentDir.isDirectory()) {
      File propFile = new File(contentDir, REALM_FILE_NAME);
      if(propFile.exists() && propFile.canRead()) {
        resourceFile = propFile.getAbsoluteFile().getAbsolutePath();
      }
    } else if(contentDir.exists() && contentDir.isFile() && contentDir.canRead()) {
      resourceFile = contentDir.getAbsoluteFile().getAbsolutePath();
    }
    if(StringUtils.isNotBlank(resourceFile)) {
      this.setResourcePath("file:"+resourceFile);
      this.destroy();
      this.init();
    }
  }
  
}
