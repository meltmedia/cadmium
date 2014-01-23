package com.meltmedia.cadmium.deployer;

import java.io.File;
import java.util.List;

/**
 * com.meltmedia.cadmium.deployer.IJBossUtil
 *
 * @author jmcentire
 */
public interface IJBossUtil {

  public void addVirtualHost(String domain) throws Exception;
  public void removeVirtualHost(String domain) throws Exception;
  public boolean isWarDeployed(String warName) throws Exception;
  public void undeploy(String warName);
  public List<String> listDeployedWars();
  public boolean isCadmiumWar(File file);
  public File getDeploymentLocation(String warName);
  public void deployWar(String warName, File warFile) throws Exception;
}
