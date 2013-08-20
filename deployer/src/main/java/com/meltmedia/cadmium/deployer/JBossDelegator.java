package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.ISJBoss;
import com.meltmedia.cadmium.core.ISOLDJBoss;
import com.meltmedia.cadmium.core.WarInfo;
import com.meltmedia.cadmium.core.util.WarUtils;
import com.meltmedia.cadmium.deployer.jboss7.JBossAdminApi;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * com.meltmedia.cadmium.deployer.JBossDelegator
 *
 * @author jmcentire
 */
@Singleton
public class JBossDelegator implements IJBossUtil {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject
  protected JBossAdminApi jboss7Api;

  @Inject
  @ISJBoss
  protected Boolean isJBoss = true;

  @Inject
  @ISOLDJBoss
  protected Boolean isOldJBoss = false;


  @Override
  public void addVirtualHost(String domain) throws Exception {
    if(isJBoss) {
      if(isOldJBoss) {
        JBossUtil.addVirtualHost(domain, logger);
      } else if(!doesVHostExist(domain)){
        jboss7Api.addVHost(domain);
      }
    }
  }

  @Override
  public void removeVirtualHost(String domain) throws Exception {
    if(isJBoss && !isOldJBoss && doesVHostExist(domain)) {
      jboss7Api.removeVHost(domain);
    }
  }

  private boolean doesVHostExist(String domain) throws Exception {
    List<String> availableVHosts = jboss7Api.listVHosts();
    return availableVHosts != null && availableVHosts.contains(domain);
  }

  @Override
  public boolean isWarDeployed(String warName) throws Exception {
    if(isJBoss) {
      if(isOldJBoss) {
        return JBossUtil.isWarDeployed(warName, logger);
      } else {
        return jboss7Api.isWarDeployed(warName);
      }
    }
    return false;
  }

  @Override
  public void undeploy(String warName) {
    if(isJBoss) {
      if(isOldJBoss) {
        JBossUtil.undeploy(warName, logger);
      } else {
        WarInfo info = null;
        try {
          File warFile = jboss7Api.getDeploymentLocation(warName);
          info = WarUtils.getWarInfo(warFile);
          jboss7Api.undeploy(warName);
          if(!jboss7Api.isWarDeployed(warName) && warFile.exists()) {
            FileUtils.forceDelete(warFile);
          }
        } catch (Exception e){
          logger.error("Failed to undeploy: "+warName, e);
        }
        if(info != null) {
          try {
            jboss7Api.removeVHost(info.getDomain());
          } catch (Throwable t) {
            logger.error("Failed to remove vHost: " + info.getDomain(), t);
          }
        } else {
          logger.warn("No war info found for war: "+warName);
        }
      }
    }
  }

  @Override
  public List<String> listDeployedWars() {
    if(isJBoss) {
      if(isOldJBoss) {
        return JBossUtil.listDeployedWars(logger);
      } else {
        try {
        return jboss7Api.listDeployedCadmiumWars();
        } catch(Exception e) {
          logger.error("Failed to list cadmium wars deployed.", e);
        }
      }
    }
    return new ArrayList<String>();
  }

  @Override
  public boolean isCadmiumWar(File file) {
    if(isJBoss) {
      if(isOldJBoss) {
        return JBossUtil.isCadmiumWar(file, logger);
      } else {
        return jboss7Api.isCadmiumWar(file);
      }
    }
    return false;
  }

  @Override
  public File getDeploymentLocation(String warName) {
    if(isJBoss) {
      if(isOldJBoss) {
        return new File(JBossUtil.getDeployDirectory(logger), warName);
      } else {
        try {
          return jboss7Api.getDeploymentLocation(warName);
        } catch(Exception e) {
          logger.error("Failed to get war: "+warName+" deployment location.", e);
        }
      }
    }
    return null;
  }

  @Override
  public void deployWar(String warName, File warFile) throws Exception {
    if(isJBoss) {
      if(isOldJBoss) {
        JBossUtil.deploy(warName, warFile, logger);
      } else {
        String locationRef = jboss7Api.uploadWar(warName, warFile);
        if(StringUtils.isNotBlank(locationRef)) {
          jboss7Api.deploy(warName, locationRef);
        } else {
          throw new Exception("Failed to deploy war: "+warName);
        }
      }
    }
  }
}
