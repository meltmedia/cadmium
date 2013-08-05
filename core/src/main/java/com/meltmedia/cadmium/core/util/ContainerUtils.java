package com.meltmedia.cadmium.core.util;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * com.meltmedia.cadmium.core.util.ContainerUtils
 *
 * @author jmcentire
 */
public class ContainerUtils {
  public static boolean isJBoss() {
    try {
      MBeanServer server = getJBossMBeanServer();
      if(server != null) {
        String domains[] = server.getDomains();
        for(String domain : domains) {
          if(domain.startsWith("jboss.")) {
            return true;
          }
        }
      }
    } catch(Throwable e) {
      e.printStackTrace();
    }
    return false;
  }

  public static boolean isOldJBoss() {
    try {
      MBeanServer server = getJBossMBeanServer();
      if(server != null) {
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("type", "Server");
        ObjectName name = new ObjectName("jboss.system", props);
        String versionName = (String) server.getAttribute(name, "VersionName");
        String versionNumber = (String) server.getAttribute(name, "VersionNumber");
        int versionMajor = new Integer(versionNumber.split("\\.")[0]);
        if(versionName.equalsIgnoreCase("EAP")) {
          if(versionMajor <= 5) {
            return true;
          }
        } else {
          if(versionMajor <= 6) {
            return true;
          }
        }
      }
    } catch(Throwable e) {
      e.printStackTrace();
    }
    return false;
  }

  private static MBeanServer getJBossMBeanServer() throws Exception {
    Class<?> mbeanLocator = Class.forName("org.jboss.mx.util.MBeanServerLocator");
    if(mbeanLocator != null) {
      Method locateJBoss = mbeanLocator.getMethod("locateJBoss");
      if(locateJBoss != null && MBeanServer.class.isAssignableFrom(locateJBoss.getReturnType())) {
        return (MBeanServer) locateJBoss.invoke(null);
      }
    }
    return null;
  }
}
