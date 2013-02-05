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

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;

/**
 * Allows an existing Shiro IniWebEnvironment to be wrapped and added to runtime. 
 * An instance of {@link PersistablePropertiesRealm} will be added to the existing 
 * configuration. This is to allow a runtime configurable realm to be added to a 
 * statically configured IniWebEnvironment.
 * @author John McEntire
 *
 */
public class WebEnvironment extends IniWebEnvironment {
  
  private PersistablePropertiesRealm persistablePropertiesRealm = new PersistablePropertiesRealm();

  @Override
  public void setWebSecurityManager(WebSecurityManager wsm) {
    List<Realm> baseRealms = new ArrayList<Realm>();
    baseRealms.addAll(((RealmSecurityManager)wsm).getRealms());
    
    baseRealms.add(persistablePropertiesRealm);
    wsm = new DefaultWebSecurityManager(baseRealms);
    super.setWebSecurityManager(wsm);
  }

  /**
   * @return The instance of PersistablePropertiesRealm that is inserted into 
   * the WebSecurityManager that is created by the IniWebEnvironment.
   */
  public PersistablePropertiesRealm getPersistablePropertiesRealm() {
    return persistablePropertiesRealm;
  }

}
