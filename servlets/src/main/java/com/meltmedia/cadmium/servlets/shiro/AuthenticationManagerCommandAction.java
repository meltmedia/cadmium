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

import com.google.inject.Inject;
import com.meltmedia.cadmium.core.ApplicationContentRoot;
import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.servlets.shiro.AuthenticationManagerRequest.RequestType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Handles updating configurable shiro realm for this site only.
 * 
 * @author John McEntire
 *
 */
public class AuthenticationManagerCommandAction implements
    CommandAction<AuthenticationManagerRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String COMMAND_NAME = "AUTHENTICATION_UPDATE";
  
  @Inject(optional=true)
  protected PersistablePropertiesRealm realm;
  
  @Inject
  protected ConfigManager configManager;
  
  @Inject
  @ApplicationContentRoot
  protected String applicationContentDir;

  @Override
  public String getName() {return COMMAND_NAME;}

  @Override
  public boolean execute(CommandContext<AuthenticationManagerRequest> ctx)
      throws Exception {
    if(ctx.getMessage().getBody() != null && realm != null) {
      String accountName = ctx.getMessage().getBody().getAccountName();
      if(ctx.getMessage().getBody().getRequestType() == RequestType.ADD) {
        String password = ctx.getMessage().getBody().getPassword();
        if(StringUtils.isNotBlank(accountName) && StringUtils.isNotBlank(password)) {
          log.debug("Added new account [{}:{}]", accountName, password);
          realm.addAccount(accountName, password);
          persistRealmChanges();
        }
      } else if(StringUtils.isNotBlank(accountName)){
        log.debug("Removing account {}", accountName);
        realm.removeUser(accountName);
        persistRealmChanges();
      }
    } else if(realm == null) {
      log.debug("No Configurable Realm found.");
    }
    return true;
  }

  /**
   * Persists the user accounts to a properties file that is only available to this site only.
   */
  private void persistRealmChanges() {
    configManager.persistProperties(realm.getProperties(), new File(applicationContentDir, PersistablePropertiesRealm.REALM_FILE_NAME), null);
  }

  @Override
  public void handleFailure(CommandContext<AuthenticationManagerRequest> ctx,
      Exception e) {}

}
