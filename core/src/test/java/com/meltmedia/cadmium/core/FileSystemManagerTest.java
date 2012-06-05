package com.meltmedia.cadmium.core;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class FileSystemManagerTest {
  
  @Before
  public void createTestDirectories() throws Exception {
    File targetDir = new File("./target/test-content");
    if(!targetDir.exists()) {
      targetDir.mkdirs();
    }
    
    File junkInDir = new File(targetDir, "test_1");
    junkInDir.mkdir();
    
    junkInDir = new File(targetDir, "test");
    junkInDir.mkdir();
    
    junkInDir = new File(targetDir, "test_2");
    junkInDir.mkdir();
    
    junkInDir = new File(targetDir, "test_3");
    junkInDir.mkdir();
    
    File newFile = new File("./target/test-content/test/index.html");
    newFile.createNewFile();
    
    newFile = new File("./target/test-content/test_1/index.html");
    newFile.createNewFile();
    
    newFile = new File(targetDir, "copy-test/level1/level2/level3");
    newFile.mkdirs();
    
    newFile = new File(targetDir, "copy-test/.git");
    newFile.mkdirs();
    
    newFile = new File(newFile, "data");
    newFile.createNewFile();
    
    newFile = new File(targetDir, "copy-test/level1/file");
    newFile.createNewFile();
    
    newFile = new File(targetDir, "copy-test/level1/level2/level3/file");
    newFile.createNewFile();
  }

  @Test
  public void testGetChildDirectoryIfExists() throws Exception {
    String existingDir = FileSystemManager.getChildDirectoryIfExists("target", "test-content");
    String nonexistingDir = FileSystemManager.getChildDirectoryIfExists("target", "non-existent-dir");
    
    assertTrue("./target/test-content Dir should exist", existingDir != null && existingDir.endsWith("test-content"));
    assertTrue("./target/non-existent-dir should not exist", nonexistingDir == null);
  }
  
  @Test
  public void testExists() throws Exception {
    assertTrue("test_2 dir should exist", FileSystemManager.exists("./target/test-content/test_2"));
    assertTrue("test_5 dir should not exist", !FileSystemManager.exists("./target/test-content/test_5"));
  }
  
  @Test
  public void testIsDirectory() throws Exception {
    assertTrue("test-content dir should be a directory", FileSystemManager.isDirector("./target/test-content"));
    assertTrue("pom.xml should not be a directory", !FileSystemManager.isDirector("pom.xml"));
  }
  
  @Test
  public void testCanRead() throws Exception {
    assertTrue("pom.xml should be readable", FileSystemManager.canRead("pom.xml"));
  }
  
  @Test
  public void testCanWrite() throws Exception {
    assertTrue("test-content directory should be readable", FileSystemManager.canWrite("./target/test-content"));
  }
  
  @Test
  public void testDeleteDeep() throws Exception {
    FileSystemManager.deleteDeep("./target/test-content");
    File targetDir = new File("./target/test-content");
    assertTrue("test-content dir should have been deleted", !targetDir.exists());
  }
  
  @Test
  public void testGetNextDirInSequence() throws Exception {
    String nextDir = FileSystemManager.getNextDirInSequence("./target/test-content/test_2");
    assertTrue("test_3 dir name not returned", nextDir != null);
    assertTrue("incorrect dir name "+nextDir, nextDir.endsWith("test_3"));
    assertTrue("test_3 dir existed prior and should have been deleted", !new File(nextDir).exists());
  }
  
  @Test
  public void testCleanUpOld() throws Exception {
    FileSystemManager.cleanUpOld("./target/test-content/test_2", 1);
    
    assertTrue("test dir should have been removed", !new File("./target/test-content/test").exists());
    assertTrue("test_1 dir should not have been removed", new File("./target/test-content/test_1").exists());
    assertTrue("test_2 dir should not have been removed", new File("./target/test-content/test_2").exists());
    assertTrue("test_3 dir should not have been removed", new File("./target/test-content/test_3").exists());
  }
  
  @Test
  public void testCopyAllContent() throws Exception {
    FileSystemManager.copyAllContent("./target/test-content/copy-test", "./target/test-content/copy-test_2", true);
    
    assertTrue("target dir did not get created.", new File("./target/test-content/copy-test_2").exists());
    assertTrue(".git dir shouldn't get created.", !new File("./target/test-content/copy-test_2/.git").exists());
    assertTrue("lowest level file didn't get created.", new File("./target/test-content/copy-test_2/level1/level2/level3/file").exists());
    assertTrue("highest level file didn't get created.", new File("./target/test-content/copy-test_2/level1/file").exists());

    FileSystemManager.copyAllContent("./target/test-content/copy-test", "./target/test-content/copy-test_3", false);
    assertTrue(".git dir didn't get created.", new File("./target/test-content/copy-test_3/.git/data").exists());
    
    
  }
}
