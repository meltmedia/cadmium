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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
  public static final String JBOSS_SERVER_HOME_PROP = "jboss.server.home.dir";
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
  
  public static void undeploy(String warName, Logger log) {
    removeWar(warName, log);
  }
  
  public static List<String> listDeployedWars(final Logger log) {
    File deployDirectory = getDeployDirectory(log);
    File wars[] = deployDirectory.listFiles(new FileFilter() {

      @Override
      public boolean accept(File file) {
        if(file.getName().endsWith(".war")) {
          return isCadmiumWar(file, log);
        }
        return false;
      }
      
    });
    List<String> cadmiumWars = new ArrayList<String>();
    for(File war : wars) {
      cadmiumWars.add(war.getName());
    }
    return cadmiumWars;
  }

  public static boolean isCadmiumWar(File file, Logger log) {
    if(file.isDirectory()) {
      return new File(file, "WEB-INF/cadmium.properties").exists();
    } else if(file.exists()){
      ZipFile zip = null;
      try {
        zip = new ZipFile(file);
        ZipEntry cadmiumProps = zip.getEntry("WEB-INF/cadmium.properties");
        return cadmiumProps != null;
      } catch (Exception e) {
        log.warn("Failed to read war file "+ file, e);
      } finally {
        if(zip != null) {
          try {
            zip.close();
          } catch (IOException e) {
            //Not much we can do about this.
          }
        }
      }
    }
    return false;
  }
  
  public static void removeWar(String warName, Logger log) {
    File deployDir = getDeployDirectory(log);
    File warFile = new File(deployDir, warName);
    if(isCadmiumWar(warFile, log)) {
      log.debug("Deleting: {}", warFile);
      warFile.delete();
    }
  }
  
  public static File getDeployDirectory(Logger log) {
    String serverDir = System.getProperty(JBOSS_SERVER_HOME_PROP);
    if(serverDir != null) {
      log.trace("Found JBoss system property: {}[{}]", JBOSS_SERVER_HOME_PROP, serverDir);
      File deployDir = new File(serverDir, "deploy");
      if(deployDir.exists() && deployDir.isDirectory()){
        log.trace("Deploy directory [{}] exists and is a file", deployDir);
        return deployDir;
      }
    }
    return null;
  }

}
