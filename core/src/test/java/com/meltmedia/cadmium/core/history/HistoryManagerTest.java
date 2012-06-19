package com.meltmedia.cadmium.core.history;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class HistoryManagerTest {
  private static final String INIT_HISTORY_CONTENT = "[{\"timestamp\":\"May 29, 2012 2:10:32 PM\",\"branch\":\"master\",\"revision\":\"initial\",\"timeLive\":1000,\"openId\":\"me\",\"servedDirectory\":\"./target/history-test\",\"revertible\":true,\"comment\":\"This is it\"}," +
  		"{\"timestamp\":\"May 29, 2012 2:10:31 PM\",\"branch\":\"master\",\"revision\":\"initial\",\"timeLive\":0,\"openId\":\"auto\",\"servedDirectory\":\"./target/history-test\",\"revertible\":false,\"comment\":\"This is an init log\"}]";
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
        try{
          writer.close();
        } catch(Exception e){}
      }
    }
    
    manager = new HistoryManager(testDirectory);
  }
  
  @Test
  public void testNoLogConstructor() throws Exception {
    new HistoryManager("./target");
  }
  
  @Test
  public void testLogEntry() throws Exception {
    long size = historyFile.length();
    
    manager.logEvent("test1", "sha1", "me", testDirectory, "This is a test", true);
    
    Thread.sleep(3000l);
    
    assertTrue("History file not written", size < historyFile.length());
    
    manager.logEvent("test2", "sha2", "me", testDirectory, "This is a test2", true);
  }
  
  @Test
  public void testGetHistory() throws Exception {
    List<HistoryEntry> history = manager.getHistory(2, false);
    
    assertTrue("The incorrect number of history items were returned.", history.size() == 2);
    System.err.println("History[0].timeLive="+history.get(0).getTimeLive() + ", History[1].timeLive=" + history.get(1).getTimeLive());
    assertTrue("History order wrong", history.get(0).getTimeLive() >= 500l && history.get(1).getTimeLive() == 0l);
    
    history = manager.getHistory(2, true);
    
    assertTrue("History didn't filter correctly", history.size() == 1);
    
    history = manager.getHistory(1, false);
    
    assertTrue("Limit didn't work["+history.size()+"]", history.size() == 1);
  }
}
