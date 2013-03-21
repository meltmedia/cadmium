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

