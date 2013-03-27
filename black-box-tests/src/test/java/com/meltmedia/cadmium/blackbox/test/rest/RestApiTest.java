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
package com.meltmedia.cadmium.blackbox.test.rest;

import com.meltmedia.cadmium.blackbox.test.ApiResponseValidator;
import com.meltmedia.cadmium.blackbox.test.CadmiumWarContainer;
import com.meltmedia.cadmium.blackbox.test.GitBareRepoInitializer;
import com.meltmedia.cadmium.blackbox.test.endpoints.*;
import com.meltmedia.cadmium.core.api.MaintenanceRequest;
import com.meltmedia.cadmium.core.util.WarUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.util.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * Test for rest api.
 *
 * @author John McEntire
 */
@RunWith(Parameterized.class)
public class RestApiTest {
  private static final Logger logger = LoggerFactory.getLogger(RestApiTest.class);
  private static CadmiumWarContainer warContainer;
  private static String token = System.getProperty("github.token");
  private static GitBareRepoInitializer gitInit;
  private static boolean skip = true;

  static {
    gitInit = new GitBareRepoInitializer();
    if(System.getProperty("api-test") != null) {
      skip = false;
    } else {
      System.out.println("Please add \"-Dapi-test=\" if you wish to run rest api tests.");
    }
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    if(!skip && StringUtils.isEmptyOrNull(token)) {
      File tokenFile = new File(System.getProperty("user.home"), ".cadmium/github.token");
      if(tokenFile.exists()) {
        try {
          token = FileUtils.readFileToString(tokenFile);
        } catch(Exception e) {
          System.err.println("Failed to attain github rest api token.");
        }
      } else {
        System.err.println("No token exists at path: " + tokenFile.getAbsolutePath());
      }
    }
    if(!skip && !StringUtils.isEmptyOrNull(token)){
      return Arrays.asList(new Object[][]{
        // System api endpoints
  /*[0] */ {new StatusPingEndpointTest()},
  /*[1] */ {new StatusHealthEndpointTest()},
  /*[2] */ {new StatusEndpointTest(token)},
  /*[3] */ {new LoggerGetEndpointTest(token)},
  /*[4] */ {new LoggerPostEndpointTest(token)},
  /*[5] */ {new LoggerNamePostEndpointTest(token)},
  /*[6] */ {new LoggerNameGetEndpointTest(token)},
  /*[7] */ {new MaintenanceEndpointTest(token, MaintenanceRequest.State.ON)},
  /*[8] */ {new MaintenanceEndpointTest(token, MaintenanceRequest.State.OFF)},
  /*[9] */ {new HistoryEndpointTest(token)},
  /*[10]*/ {new HistoryLimitEndpointTest(token)},
  /*[11]*/ {new HistoryFilterEndpointTest(token)},
  /*[12]*/ {new HistoryLimitFilterEndpointTest(token)},
  /*[13]*/ {new UpdateEndpointTest(token, gitInit)},
  /*[14]*/ {new UpdateConfigEndpointTest(token, gitInit)},
  /*[15]*/ {new ApiACLEndpointTest(token)},
  /*[16]*/ {new AuthenticationManagerEndpointTest(token)},
        // Client api endpoints
  /*[17]*/ {new SearchEndpointTest()}
    });
    } else {
      return Arrays.asList(new Object[][]{});
    }
  }

  @BeforeClass
  public static void deployWar() throws Exception {
    if(!skip && !StringUtils.isEmptyOrNull(token)) {
      gitInit.init(new File("./target/test-content-repo").getAbsoluteFile().getAbsolutePath()
          , new File("./target/filtered-resources/test-content").getAbsoluteFile().getAbsolutePath()
          , new File("./target/filtered-resources/test-config").getAbsoluteFile().getAbsolutePath());
      System.setProperty("com.meltmedia.cadmium.contentRoot", new File("target/filtered-resources").getAbsoluteFile().getAbsolutePath());
      WarUtils.updateWar(null, "target/deploy/cadmium-war.war"
          , Arrays.asList("target/deploy/cadmium.war")
          , new File("./target/test-content-repo").getAbsoluteFile().getAbsolutePath(), "cd-master"
          , new File("./target/test-content-repo").getAbsoluteFile().getAbsolutePath(), "cfg-master"
          , "localhost", "/", true, logger);
      warContainer = new CadmiumWarContainer("target/deploy/cadmium.war", 8901);
      warContainer.setupCadmiumEnvironment("target", "testing");
      warContainer.startServer();
      while(!warContainer.isStarted()) {
        Thread.sleep(500l);
      }
    } else {
      gitInit = null;
    }
  }

  @AfterClass
  public static void destroyWarContainer() throws Exception {
    if(warContainer != null) {
      warContainer.stopServer();
    }
    if(gitInit != null) {
      gitInit.close();
    }
  }

  private EndpointTest test;
  private ApiResponseValidator validator;
  public RestApiTest(EndpointTest test) {
    this.test = test;
  }

  @Test
  public void test() throws Exception {
    System.out.println("Running test: "+test);
    test.preTest();
    test.getValidator().validate(test.getRequest().makeRequest());
    test.postTest();
  }
}



