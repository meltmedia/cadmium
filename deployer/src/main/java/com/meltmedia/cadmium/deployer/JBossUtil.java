package com.meltmedia.cadmium.deployer;

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

}
