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
package com.meltmedia.cadmium.core.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.meltmedia.cadmium.core.commands.GitLocation;
import com.meltmedia.cadmium.core.history.HistoryEntry.EntryType;

public class HistoryManagerTest {
  private static final String INIT_HISTORY_CONTENT = "[{\"timestamp\":\"May 29, 2012 2:10:32 PM\",\"repoUrl\":\"\",\"branch\":\"master\",\"revision\":\"initial\",\"timeLive\":1000,\"openId\":\"me\",\"servedDirectory\":\"./target/history-test\",\"revertible\":true,\"comment\":\"This is it\"}," +
  		"{\"timestamp\":\"May 29, 2012 2:10:31 PM\",\"repoUrl\":\"\",\"branch\":\"master\",\"revision\":\"initial\",\"timeLive\":0,\"openId\":\"auto\",\"servedDirectory\":\"./target/history-test\",\"revertible\":false,\"comment\":\"This is an init log\"}]";
  private String testDirectory = null; 
  private File historyFile = null;
  private HistoryManager manager;

  @Before
  public void setupForTest() throws Exception {
    File testDir = new File("./target/history-test");
    testDirectory = testDir.getAbsoluteFile().getAbsolutePath();
    
    if(!testDir.exists()) {
      testDir.mkdirs();
    }
    
    historyFile = new File(testDir, HistoryManager.HISTORY_FILE_NAME);
    
    if(!historyFile.exists() || historyFile.canWrite()) {
      FileWriter writer = null;
      try{
        writer = new FileWriter(historyFile, false);
        writer.write(INIT_HISTORY_CONTENT);
        writer.flush();
      } catch(Exception e){
      } finally {
        IOUtils.closeQuietly(writer);
      }
    }
    
    manager = new HistoryManager(testDirectory, Executors.newSingleThreadExecutor());
  }
  
  @After
  public void cleanUp() throws Exception {
    historyFile.delete();
  }
  
  @Test
  public void testNoLogConstructor() throws Exception {
    new HistoryManager("./target");
  }
  
  @Test
  public void testLogEntry() throws Exception {
    long size = historyFile.length();
    
    manager.logEvent(EntryType.CONTENT, new GitLocation("", "test1", "sha1"), "me", testDirectory, null, "This is a test", true, true);
    
    Thread.sleep(3000l);
    
    assertTrue("History file not written", size < historyFile.length());
    
    manager.logEvent(EntryType.CONTENT, new GitLocation("", "test2", "sha2"), "me", testDirectory, null, "This is a test2", true, true);
  }
  
  @Test
  public void testGetHistory() throws Exception {
    List<HistoryEntry> history = manager.getHistory(2, false);
    
    assertEquals("The incorrect number of history items were returned.", 2, history.size());
    System.err.println("History[0].timeLive="+history.get(0).getTimeLive() + ", History[1].timeLive=" + history.get(1).getTimeLive());
    assertTrue("History order wrong", history.get(0).getTimeLive() >= 500l && history.get(1).getTimeLive() == 0l);
    
    history = manager.getHistory(2, true);
    
    assertTrue("History didn't filter correctly", history.size() == 1);
    
    history = manager.getHistory(1, false);
    
    assertTrue("Limit didn't work["+history.size()+"]", history.size() == 1);
  }
}
