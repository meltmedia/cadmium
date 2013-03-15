package com.meltmedia.cadmium.blackbox.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;

/**
 * Utility class that launches a cadmium war in the background in the current jvm.
 *
 * @author John McEntire
 */
public class CadmiumWarContainer {

  private Server embeddedServer;

  public CadmiumWarContainer(String warPath, int port) throws Exception {
    embeddedServer = new Server(port);
    WebAppContext warContext = new WebAppContext(warPath, "/");
    File aFile = new File(warPath).getAbsoluteFile();
    File webappFile = new File(aFile.getParent(), "webapp");
    Runtime.getRuntime().exec(new String[]{"ln", "-s", aFile.getAbsolutePath(), webappFile.getAbsolutePath()}).waitFor();
    embeddedServer.setHandler(warContext);
  }

  public void setupCadmiumEnvironment(String basePath, String env) {
    System.setProperty("com.meltmedia.cadmium.contentRoot", basePath);
    System.setProperty("jboss.server.home.dir", basePath);
    System.setProperty("com.meltmedia.cadmium.github.sshKey", new File(System.getProperty("user.home"), ".ssh").getAbsoluteFile().getAbsolutePath());
    System.setProperty("com.meltmedia.cadmium.environment", env);
    System.setProperty("com.meltmedia.cadmium.teams.properties", "target/filtered-resources/teams.properties");
    System.setProperty("com.meltmedia.cadmium.maven.repository", "http://nexus.meltmedia.com/content/repositories/snapshots");
  }

  public void startServer() throws Exception {
    embeddedServer.start();
  }

  public boolean isStarted() throws Exception {
    if(embeddedServer.isStarted()) {
      return true;
    } else if(embeddedServer.isFailed()) {
      throw new Exception("War failed to deploy.");
    }
    return false;
  }

  public void stopServer() throws Exception {
    if(!(embeddedServer.isStopped() || embeddedServer.isStopping())) {
      embeddedServer.stop();
    }
  }
}

