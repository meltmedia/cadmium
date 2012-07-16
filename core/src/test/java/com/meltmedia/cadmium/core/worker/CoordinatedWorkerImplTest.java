/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.worker;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jgroups.stack.IpAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.lifecycle.UpdateState;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;

public class CoordinatedWorkerImplTest {
  private GitService service;
  private File baseDir;
  
  @Before
  public void setupForTest() throws Exception {
    if(FileSystemManager.exists("./target/worker-test")) {
      FileSystemManager.deleteDeep("./target/worker-test");
    }
    baseDir = new File("./target/worker-test");
    baseDir.mkdirs();
    service = GitService.cloneRepo("git://github.com/meltmedia/test-content-repo.git", new File(baseDir, "git-checkout").getAbsolutePath());
    
    File renderedContent = new File(baseDir, "renderedContent");
    renderedContent.mkdir();
    
    renderedContent = new File(baseDir, "renderedContent_1");
    renderedContent.mkdir();
    
    renderedContent = new File(baseDir, "renderedContent_2");
    renderedContent.mkdir();
    
    renderedContent = new File(baseDir, "renderedContent_3");
    renderedContent.mkdir();
    
  }
  
  @After
  public void shutdownService() throws Exception {
    service.close();
  }
  
  @Test
  public void testBeginPullUpdates() throws Exception {
    Properties configProperties = new Properties();
    configProperties.setProperty("com.meltmedia.cadmium.lastUpdated", new File(baseDir, "renderedContent_3").getAbsolutePath());
    
    Map<String,String> properties = new HashMap<String, String>();
    properties.put("branch", "cd-dev-testing");
    properties.put("sha", "41fb29368e8649c1ee2ea74228414553dd1f2d45");
    
    DummyMessageSender sender = new DummyMessageSender();
    
    LifecycleService lifecycleService = new LifecycleService();
    
    lifecycleService.setSender(sender);
    
    List<ChannelMember> members = new ArrayList<ChannelMember>();
    members.add(new ChannelMember(new IpAddress(1234), true, true, UpdateState.UPDATING));
    lifecycleService.setMembers(members);
    
    
    CoordinatedWorkerImpl worker = new CoordinatedWorkerImpl();
    worker.configProperties = configProperties;
    worker.sender = sender;
    worker.service = service;
    worker.lifecycleService = lifecycleService;
    
    worker.beginPullUpdates(properties);
    int timeout = 5000;
    Thread.sleep(500l);
    while(timeout > 0 && !worker.lastTask.isDone()) {
      timeout--;
      Thread.sleep(500l);
    }
    
    assertTrue("Work not done", worker.lastTask.isDone());
    assertTrue("Work shouldn't have been cancelled", !worker.lastTask.isCancelled());
    
    assertTrue("Work not success", worker.lastTask.get());
    assertTrue("State not updated to waiting", members.get(0).getState() == UpdateState.WAITING);
  }
 }
