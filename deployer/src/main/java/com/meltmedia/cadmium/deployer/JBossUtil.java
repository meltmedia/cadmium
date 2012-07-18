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
package com.meltmedia.cadmium.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.catalina.Host;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;

public class JBossUtil {
  public static void addVirtualHost(String domain, Logger log) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
    MBeanServer server = MBeanServerLocator.locateJBoss();
    boolean aliasFound = server.isRegistered(new ObjectName("jboss.web:type=Host,host="+domain));
    if( !aliasFound ) {
      log.info("Adding vHost {} to jboss", domain);
      Host host = (Host) server.instantiate("org.apache.catalina.core.StandardHost");
      host.setName(domain);

      server.invoke(new ObjectName("jboss.web:type=Engine"), "addChild", new Object[] { host }, new String[] { "org.apache.catalina.Container" });
    }
  }
  
  public static void undeploy(String warName, Logger log) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, ReflectionException, MBeanException, AttributeNotFoundException {
    MBeanServer server = MBeanServerLocator.locateJBoss();
    ObjectName warObj = new ObjectName("jboss.web.deployment:war="+warName);
    if(server.isRegistered(warObj)){
      log.debug("{} is registered, Deleting!", warName);
      removeWar(warName, log);
      log.debug("{} Stopping!", warName);
      server.invoke(warObj, "stop", new Object [] {}, new String [] {});
      log.debug("{} Destroying!", warName);
      server.invoke(warObj, "destroy", new Object [] {}, new String [] {});
      log.debug("{} Unregistering!", warName);
      server.unregisterMBean(warObj);
    }
  }
  
  public static List<String> listDeployedWars(Logger log) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
    MBeanServer server = MBeanServerLocator.locateJBoss();
    List<String> deployed = new ArrayList<String>();
    Set<ObjectName> names = server.queryNames(new ObjectName("jboss.web.deployment:war=*"), null);
    if(names != null) {
      for(ObjectName name : names) {
        log.debug("Checking {}", name);
        if(name.toString().length() > 25) {
          String warName = name.toString().substring(25);
          ObjectName moduleName = createWebModuleObjectName(warName); 
          log.debug("Checking if WebModule {} is registered", moduleName);
          if(server.isRegistered(moduleName)){
            Object docBase = server.getAttribute(moduleName, "docBase");
            log.debug("Found docBase {}", docBase);
            File cadmiumPropsFile = new File(docBase + "", "/WEB-INF/cadmium.properties");
            if(cadmiumPropsFile.exists()) {
              log.debug("{} is cadmium war.", warName);
              deployed.add(warName);
            }
          }
        }
      }
    }
    return deployed;
  }

  public static ObjectName createWebModuleObjectName(String warName)
      throws MalformedObjectNameException {
    ObjectName moduleName = new ObjectName("jboss.web:j2eeType=WebModule,name=//" + (warName.startsWith("/") ? "localhost" : "" ) + warName + ",J2EEApplication=none,J2EEServer=none");
    return moduleName;
  }
  
  public static void removeWar(String contextName, Logger log) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, MalformedObjectNameException, NullPointerException {
    MBeanServer server = MBeanServerLocator.locateJBoss();
    ObjectName moduleName = createWebModuleObjectName(contextName); 
    if(server.isRegistered(moduleName)){
      log.debug("{} is registered", moduleName);
      Object docBase = server.getAttribute(moduleName, "docBase");
      if(docBase != null) {
        String splitPath[] = docBase.toString().split("/");
        if(splitPath != null && splitPath.length > 0) {
          String warName = splitPath[splitPath.length - 1];
          if(warName != null) {
            log.debug("Warname: {}", warName);
            String deployDir = System.getProperty("jboss.server.home.dir", "/opt/jboss/server/meltmedia") + "/deploy";
            File warFile = new File(deployDir, warName);
            if(warFile.exists()) {
              log.debug("Deleting: {}", warFile);
              warFile.delete();
            }
          }
        }
      }
    }
  }
  
  

}
