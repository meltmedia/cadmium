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
