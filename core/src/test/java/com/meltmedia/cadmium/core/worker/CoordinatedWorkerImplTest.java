package com.meltmedia.cadmium.core.worker;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;

public class CoordinatedWorkerImplTest {
  private GitService service;
  private File baseDir;
  
  @Before
  public void setupForTest() throws Exception {
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
    configProperties.setProperty("com.meltmedia.cadmium.lastUpdated", new File(baseDir, "renderedContent_4").getAbsolutePath());
    
    Map<String,String> properties = new HashMap<String, String>();
    properties.put("branch", "other-branch");
    properties.put("sha", "41fb29368e8649c1ee2ea74228414553dd1f2d45");
    
    DummyMessageSender sender = new DummyMessageSender();
    
    CoordinatedWorkerImpl worker = new CoordinatedWorkerImpl();
    worker.configProperties = configProperties;
    worker.sender = sender;
    worker.service = service;
    
    worker.beginPullUpdates(properties);
    int timeout = 5000;
    while(timeout > 0 && !worker.lastTask.isDone()) {
      timeout--;
      Thread.sleep(500l);
    }
    
    assertTrue("Work not done", worker.lastTask.isDone());
    assertTrue("Work shouldn't have been cancelled", !worker.lastTask.isCancelled());
    
    assertTrue("Work not success", worker.lastTask.get());
  }
 }
