package com.meltmedia.cadmium.core.git;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GitServiceTest {
  private File testDir;
  private File gitRepo1;
  private GitService git1;
  private File gitRepo2;
  private GitService git2;
  
  @Before
  public void createDirForTests() throws Exception {
    testDir = new File("./target/git-test");
    if(!testDir.exists()) {
      if(testDir.mkdir()){
        gitRepo1 = new File(testDir, "checkout1");
        git1 = GitService.cloneRepo("git://github.com/meltmedia/test-content-repo.git", gitRepo1.getAbsolutePath());
        
        gitRepo2 = new File(testDir, "checkout2");
        git2 = GitService.cloneRepo("git://github.com/meltmedia/test-content-repo.git", gitRepo2.getAbsolutePath());
      } else {
        throw new Exception("Failed to set up tests");
      }
    } else {
      gitRepo1 = new File(testDir, "checkout1");
      git1 = GitService.createGitService(new File(gitRepo1, ".git").getAbsolutePath());
      
      gitRepo2 = new File(testDir, "checkout2");
      git2 = GitService.createGitService(new File(gitRepo2, ".git").getAbsolutePath());
    }
  }
  
  @After
  public void closeGitServices() throws Exception {
    git1.close();
    git2.close();
  }
  
  @Test
  public void testPull() throws Exception {
    assertTrue("Failed to pull git checkout1", git1.pull());
    assertTrue("Failed to pull git checkout2", git2.pull());
  }
  
  @Test
  public void testSwitchBranch() throws Exception {
    String currentBranch = "master";
    String nextBranch = "other-branch";
    
    git1.switchBranch(nextBranch);
    assertTrue("Branch didn't change ["+git1.getBranchName()+"]", git1.getBranchName().equals(nextBranch));
    git1.switchBranch(currentBranch);
    assertTrue("Branch didn't change back ["+git1.getBranchName()+"]", git1.getBranchName().equals(currentBranch));
  }
  
  @Test
  public void testResetToRev() throws Exception {
    String currentRev = git2.getCurrentRevison();
    String prevRev = "41fb29368e8649c1ee2ea74228414553dd1f2d45";
    
    git2.resetToRev(prevRev);
    assertTrue("Revision not reset to previous revision ["+git2.getCurrentRevison()+"]", git2.getCurrentRevison().equals(prevRev));
    git2.resetToRev(currentRev);
    assertTrue("Revision not reset to current revision ["+git2.getCurrentRevison()+"]", git2.getCurrentRevison().equals(currentRev));
  }
  
  @Test
  public void testCloneDiskBasedRepo() throws Exception {
    GitService git = GitService.cloneRepo(git1.getRepositoryDirectory(), new File(testDir, "cloned").getAbsolutePath());
    assertTrue("New Git service not created", git != null);
    git.close();
    assertTrue("Failed to create new directory", new File(testDir, "cloned").exists());
  }
}

