package com.meltmedia.cadmium.core.util;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
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
            System.out.println("Running on JBoss AS");
            return true;
          }
        }
      }
    } catch(Throwable e) {
      System.out.println("No JBoss AS MBean service found.");
      try {
        Context ctx = new InitialContext();
        NamingEnumeration<NameClassPair> names = ctx.list("java:jboss/jaas");
        boolean foundJBoss = names.hasMore();
        return names.hasMore();
      } catch (Exception e1) {
        System.out.println("Not running on JBoss AS.");
      }
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
            System.out.println("Running on old JBoss");
            return true;
          }
        } else {
          if(versionMajor <= 6) {
            System.out.println("Running on old JBoss");
            return true;
          }
        }
      }
    } catch(Throwable e) {
      System.out.println("Not running on OLD JBoss AS.");
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
