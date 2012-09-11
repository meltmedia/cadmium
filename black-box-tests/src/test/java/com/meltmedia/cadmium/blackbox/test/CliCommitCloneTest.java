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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.meltmedia.cadmium.blackbox.test.CadmiumAssertions.assertContentDeployed;
import static com.meltmedia.cadmium.blackbox.test.CadmiumAssertions.assertUrlStatus;
import static org.junit.Assert.assertEquals;

/**
 * <p>Tests Cadmium's commit command as a separated black box test.</p>
 * 
 * @author John McEntire
 *
 */
public class CliCommitCloneTest {
  
  /**
   * <p>The path to the directory containing the test content to commit.</p>
   */
  private static final String TEST_CONTENT_LOCATION = "target/filtered-resources/test-content";
  
  /**
   * <p>The URL for the site to commit to.</p>
   */
  private static final String DEPLOY_URL = "http://localhost";
  public static URI BASE_URI;
  
  @BeforeClass
  public static void beforeClass() {
    BASE_URI = URI.create(System.getProperty("baseUri")); 
  }
  
  @AfterClass
  public static void afterClass() {
    BASE_URI = null;
  }

  /**
   * <p>Runs the Cadmium CLI command commit from Apache Commons Exec library. Then uses {@link CadmiumAssertions#assertContentDeployed(String, java.io.File, String)} to assert that the content deployed properly.</p>
   * <p>To override the site url, pass <code>-Dcom.meltmedia.cadmium.test.site.url=[URL]</code> to the jvm that this test case will be running in. The site url defaults to <code>http://localhost</code> if not set.</p>
   * @throws IOException See {@link DefaultExecutor#execute(CommandLine)} for more information.
   * @throws ExecuteException See {@link DefaultExecutor#execute(CommandLine)} for more information.
   * @throws InterruptedException 
   * 
   * @see <a href="http://commons.apache.org/exec">Apache Commons Exec</a>
   */
  @Test
  public void testCommit() throws ExecuteException, IOException {
    String deployUrl = System.getProperty("com.meltmedia.cadmium.test.site.url", DEPLOY_URL);
    CommandLine commitCmd = CommandLine.parse("cadmium commit -m \"Testing commit command\" " + TEST_CONTENT_LOCATION + " " + deployUrl);
    
    DefaultExecutor exec = new DefaultExecutor();
    exec.setExitValue(0);
    ExecuteWatchdog watchDog = new ExecuteWatchdog(60000);
    exec.setWatchdog(watchDog);
    
    int exitValue = exec.execute(commitCmd);
    
    
    assertEquals("Commit command returned with an error status.", exitValue, 0);
    
    assertContentDeployed("Failed to commit content to remote site.", new File(TEST_CONTENT_LOCATION), DEPLOY_URL);
  }
  
  @Test
  public void testDeployAndUndeploy() throws Exception {
    // When the URL is undeployed, it should be a 501.
    assertUrlStatus("Content URL did not return 501 before test.", BASE_URI.resolve("/index.html").toString(), 501);
    
    // deploy the content to the test domain.
    CommandResult result = execute("cadmium deploy git@github.com:meltmedia/test-content-repo.git "+BASE_URI.toString());
    assertEquals("cadmium deploy did not return 0.", 0, result.exitValue);
    
    // verify that the result is now 200.
    assertUrlStatus("Content URL did not return 503 after deploy.", BASE_URI.resolve("/index.html").toString(), 200);
 
    // undeploy the content.
    result = execute("cadmium undeploy "+BASE_URI.toString());
    assertEquals("cadmium undeploy did not return 0.", 0, result.exitValue);
    
    // When the URL is undeployed, it should be a 501.
    assertUrlStatus("Content URL did not return 501 before test.", BASE_URI.resolve("/index.html").toString(), 501);
  }
  
  @Test
  public void testDeployBranch() throws Exception {
    
  }
  
  public static CommandResult execute(String command) throws ExecuteException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLine commitCmd = CommandLine.parse(command);
    
    DefaultExecutor exec = new DefaultExecutor();
    exec.setExitValue(0);
    exec.setStreamHandler(new PumpStreamHandler(out, err));
    exec.setWatchdog(new ExecuteWatchdog(60000));

    CommandResult result = new CommandResult();
    result.exitValue = exec.execute(commitCmd);
    result.out = out.toString("UTF-8");
    result.err = err.toString("UTF-8");
    return result;
  }
  
  public static class CommandResult {
    public int exitValue;
    public String out;
    public String err;
  }
}
