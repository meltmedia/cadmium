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
import com.meltmedia.cadmium.blackbox.test.endpoints.*;
import com.meltmedia.cadmium.core.api.MaintenanceRequest;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;

/**
 * Test for rest api.
 *
 * @author John McEntire
 */
@RunWith(Parameterized.class)
public class RestApiTest {

  private static CadmiumWarContainer warContainer;
  private static String token;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    File tokenFile = new File(System.getProperty("user.home"), ".cadmium/github.token");
    if(tokenFile.exists()) {
      try {
        token = FileUtils.readFileToString(tokenFile);
      } catch(Exception e) {
        fail("Failed to attain github rest api token.");
      }
    } else {
      fail("No token exists at path: "+tokenFile.getAbsolutePath());
    }
    return Arrays.asList(new Object[][]{
      {new StatusPingEndpointTest()},
      {new StatusHealthEndpointTest()},
      {new StatusEndpointTest(token)},
      {new MaintenanceEndpointTest(token, MaintenanceRequest.State.ON)},
      {new MaintenanceEndpointTest(token, MaintenanceRequest.State.OFF)}
    });
  }

  @BeforeClass
  public static void deployWar() throws Exception {
    warContainer = new CadmiumWarContainer("target/deploy/cadmium-war.war", 8901);
    warContainer.setupCadmiumEnvironment("target", "testing");
    warContainer.startServer();
    while(!warContainer.isStarted()) {
      Thread.sleep(500l);
    }
  }

  @AfterClass
  public static void destroyWarContainer() throws Exception {
    if(warContainer != null) {
      warContainer.stopServer();
    }
  }

  private EndpointTest test;
  private ApiResponseValidator validator;
  public RestApiTest(EndpointTest test) {
    this.test = test;
  }

  @Test
  public void test() throws Exception {
    test.preTest();
    test.getValidator().validate(test.getRequest().makeRequest());
    test.postTest();
  }
}



