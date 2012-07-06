package com.meltmedia.cadmium.deployer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.Host;
import org.jboss.mx.util.MBeanServerLocator;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.cli.InitializeWarCommand;

public class JGroupsMessagingListener implements ServletContextListener, Receiver {
  public static final String JGROUPS_CHANNEL = "com.meltmedia.cadmium.deployer.jgroups.channel";
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private JChannel channel;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    String channelName = "Cadmium-Deployer-" + System.getProperty("com.meltmedia.cadmium.environment", "dev");
    String configFile = System.getProperty("com.meltmedia.cadmium.jgroups.channel.config");
    try {
      if(configFile != null) {
        URL configUrl = null;
        try {
          configUrl = new URL(configFile);
        } catch(Exception e) {
          configUrl = new URL("file://" + new File(configFile).getAbsoluteFile().getAbsolutePath());
        }
        channel = new JChannel(configUrl);
      } else {
        channel = new JChannel(JGroupsMessagingListener.class.getClassLoader().getResource("tcp.xml"));
      }
      channel.connect(channelName);
      channel.setReceiver(this);
      
      sce.getServletContext().setAttribute(JGROUPS_CHANNEL, channel);
    } catch(Exception e) {
      logger.warn("Failed to connection to jgroups.", e);
    }
    
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    if(channel != null) {
      try {
        channel.close();
        channel = null;
      } catch(Throwable t) {
        logger.warn("Failed to close jgroups channel.", t);
        channel = null;
      }
    }
  }

  @Override
  public void receive(Message msg) {
    try {
      Map<String, String> params = new Gson().fromJson(msg.getObject().toString(), new TypeToken<Map<String, String>>(){}.getType());
  
      InitializeWarCommand initCommand = new InitializeWarCommand();
      
      logger.info("Beginning war creation. branch: {}, repo {}, domain {}, context {}", new String[]{params.get("branch"), params.get("repo"), params.get("domain"), params.get("context")});
      
      initCommand.setBranch(params.get("branch"));
      if(params.containsKey("domain") && params.get("domain").length() > 0) {
        try {
          MBeanServer server = MBeanServerLocator.locateJBoss();
          boolean aliasFound = server.isRegistered(new ObjectName("jboss.web:type=Host,host="+params.get("domain")));
          if( !aliasFound ) {
            logger.info("Adding vHost {} to jboss", params.get("domain"));
            Host host = (Host) server.instantiate("org.apache.catalina.core.StandardHost");
            host.setName(params.get("domain").split(".")[0]);
            host.addAlias(params.get("domain"));

            server.invoke(new ObjectName("jboss.web:type=Engine"), "addChild", new Object[] { host }, new String[] { "org.apache.catalina.Container" });
          }
        } catch(Throwable t) {
          logger.warn("Failed to add vHost", t);
        }
        initCommand.setDomain(params.get("domain"));
      }
      if(params.containsKey("context") && params.get("context").length() > 0) {
        initCommand.setContext(params.get("context"));
      }
      initCommand.setRepoUri(params.get("repo"));
      
      
      //setup war name and inset into list in initCommand
      List<String> newWarNames = new ArrayList<String>();
      String tmpFileName = params.get("domain").replace("\\.", "_") + ".war";   
      File tmpZip = File.createTempFile(tmpFileName, null);
      tmpZip.delete();
      newWarNames.add(tmpZip.getAbsolutePath());
      initCommand.setNewWarNames(newWarNames);
      initCommand.execute();
      
      String deployPath = System.getProperty("jboss.server.home.dir", "/opt/jboss/server/meltmedia") + "/deploy";
      
      tmpZip.renameTo(new File(deployPath, tmpFileName));
    } catch(Exception e) {
      logger.error("Failed to deploy new cadmium war", e);
    }
  }

  @Override
  public void block() {}

  @Override
  public void suspect(Address arg0) {
    logger.debug("Deployer node is suspected of being down {}", arg0);
  }

  @Override
  public void viewAccepted(View arg0) {
    logger.debug("Deployer; new view accepted {}", arg0);
  }

  @Override
  public void getState(OutputStream arg0) throws Exception {
    
  }

  @Override
  public void setState(InputStream arg0) throws Exception {
    
  }

  @Override
  public void unblock() {
    
  }

}
