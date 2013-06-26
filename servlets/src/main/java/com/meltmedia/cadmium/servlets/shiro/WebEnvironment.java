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

import org.apache.shiro.config.Ini;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.config.IniFilterChainResolverFactory;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.filter.mgt.DefaultFilter;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows an existing Shiro IniWebEnvironment to be wrapped and added to runtime. 
 * An instance of {@link PersistablePropertiesRealm} will be added to the existing 
 * configuration. This is to allow a runtime configurable realm to be added to a 
 * statically configured IniWebEnvironment.
 * @author John McEntire
 *
 */
public class WebEnvironment extends IniWebEnvironment {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  public static final String TRUSTED_SECTION_NAME = "trusted-hosts";
  private PersistablePropertiesRealm persistablePropertiesRealm = new PersistablePropertiesRealm();
  protected List<String> trustedHosts = new ArrayList<String>();

  @Override
  public void setWebSecurityManager(WebSecurityManager wsm) {
    List<Realm> baseRealms = new ArrayList<Realm>();
    baseRealms.addAll(((RealmSecurityManager)wsm).getRealms());
    
    baseRealms.add(persistablePropertiesRealm);
    wsm = new DefaultWebSecurityManager(baseRealms);
    super.setWebSecurityManager(wsm);
  }

  @Override
  protected FilterChainResolver createFilterChainResolver() {
    Ini.Section section = this.getIni().getSection(TRUSTED_SECTION_NAME);
    trustedHosts = new ArrayList<String>();
    if (!CollectionUtils.isEmpty(section)) {
      logger.debug("Found " + TRUSTED_SECTION_NAME + " ini section in shiro.ini");
      for (String key : section.keySet()) {
        logger.debug("Adding " + section.get(key) + " to list of trusted ip addresses.");
        trustedHosts.add(section.get(key).trim());
      }
    }
    if(!CollectionUtils.isEmpty(trustedHosts)){
      Ini.Section filterConfigs = getIni().getSection(IniFilterChainResolverFactory.FILTERS);
      if(CollectionUtils.isEmpty(filterConfigs)) {
        filterConfigs = getIni().addSection(IniFilterChainResolverFactory.FILTERS);
      }
      if(!filterConfigs.containsKey(DefaultFilter.authcBasic.name())) {
        filterConfigs.put(DefaultFilter.authcBasic.name(), "com.meltmedia.cadmium.servlets.shiro.TrustedBasicHttpAuthenticationFilter");
        String trustedHostStr = "";
        for(String host : trustedHosts) {
          if(trustedHostStr.length() > 0) {
            trustedHostStr += ",";
          }
          trustedHostStr += host;
        }
        filterConfigs.put(DefaultFilter.authcBasic.name()+".trustedHosts", trustedHostStr);
      }
    }
    return super.createFilterChainResolver();
  }

  /**
   * @return The instance of PersistablePropertiesRealm that is inserted into 
   * the WebSecurityManager that is created by the IniWebEnvironment.
   */
  public PersistablePropertiesRealm getPersistablePropertiesRealm() {
    return persistablePropertiesRealm;
  }

}
