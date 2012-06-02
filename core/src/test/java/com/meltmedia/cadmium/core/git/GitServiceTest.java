package com.meltmedia.cadmium.core.git;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GitServiceTest {
  private File testDir;
  private File gitRepo1;
  private GitService git1;
  private File gitRepo2;
  private GitService git2;
  private File localGitRepo;
  private GitService localGit;
  private File localGitRepoCloned;
  private GitService localClone;
  
  @Before
  public void createDirForTests() throws Exception {
    testDir = new File("./target/git-test");
    if(!testDir.exists()) {
      if(testDir.mkdir()){
        gitRepo1 = new File(testDir, "checkout1");
        git1 = GitService.cloneRepo("git://github.com/meltmedia/test-content-repo.git", gitRepo1.getAbsolutePath());
        
        gitRepo2 = new File(testDir, "checkout2");
        git2 = GitService.cloneRepo("git://github.com/meltmedia/test-content-repo.git", gitRepo2.getAbsolutePath());
        localGitRepo = new File(testDir, "local-git");
        localGitRepo.mkdirs();
        new File(localGitRepo, "delete.me").createNewFile();
        
        localGit = new GitService(Git.init().setDirectory(localGitRepo).call());
        localGit.git.add().addFilepattern("delete.me").call();
        localGit.git.commit().setMessage("initial commit").call();
        
        localGitRepoCloned = new File(testDir, "local-git-cloned");
        localClone = GitService.cloneRepo(new File(localGitRepo, ".git").getAbsoluteFile().getAbsolutePath(), localGitRepoCloned.getAbsoluteFile().getAbsolutePath());
        
      } else {
        throw new Exception("Failed to set up tests");
      }
    } else {
      gitRepo1 = new File(testDir, "checkout1");
      git1 = GitService.createGitService(new File(gitRepo1, ".git").getAbsolutePath());
      
      gitRepo2 = new File(testDir, "checkout2");
      git2 = GitService.createGitService(new File(gitRepo2, ".git").getAbsolutePath());
      
      localGitRepo = new File(testDir, "local-git");
      localGit = GitService.createGitService(new File(localGitRepo, ".git").getAbsolutePath());
      
      localGitRepoCloned = new File(testDir, "local-git-cloned");
      localClone = GitService.createGitService(new File(localGitRepoCloned, ".git").getAbsolutePath());
    }
  }
  
  @After
  public void closeGitServices() throws Exception {
    git1.close();
    git2.close();
    localGit.close();
    localClone.close();
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
    String currentRev = git2.getCurrentRevision();
    String prevRev = "41fb29368e8649c1ee2ea74228414553dd1f2d45";
    
    git2.resetToRev(prevRev);
    assertTrue("Revision not reset to previous revision ["+git2.getCurrentRevision()+"]", git2.getCurrentRevision().equals(prevRev));
    git2.resetToRev(currentRev);
    assertTrue("Revision not reset to current revision ["+git2.getCurrentRevision()+"]", git2.getCurrentRevision().equals(currentRev));
  }
  
  @Test
  public void testCloneDiskBasedRepo() throws Exception {
    GitService git = GitService.cloneRepo(git1.getRepositoryDirectory(), new File(testDir, "cloned").getAbsolutePath());
    assertTrue("New Git service not created", git != null);
    git.close();
    assertTrue("Failed to create new directory", new File(testDir, "cloned").exists());
  }
  
  @Test
  public void testNewRemoteBranch() throws Exception {
    
    assertTrue("Branch shouldn't yet exist", localGit.repository.getRef("newBranch") == null);
    
    assertTrue("Branch should have been created", localClone.newRemoteBranch("newBranch"));
    
    assertTrue("Branch did not get created in remote", localGit.repository.getRef("newBranch") != null);
    
    assertTrue("Branch did not get created in local", localClone.repository.getRef("newBranch") != null);
    
    assertTrue("Branch shouldn't allow me to create it", !localClone.newRemoteBranch("newBranch"));
  }
  
  @Test
  public void testInitializeContentDirectory() throws Exception {
    GitService cloned = null;
    try {
      cloned = GitService.initializeContentDirectory(localGitRepo.getAbsolutePath(), "master", new File(testDir, "content").getAbsolutePath(), "cadmium.war");
    } finally {
      if(cloned != null) {
        cloned.close();
      }
    }
    assertTrue("Initialize method failed", cloned != null);
    cloned = null;
    try {
      cloned = GitService.initializeContentDirectory(localGitRepo.getAbsolutePath(), "master", new File(testDir, "content").getAbsolutePath(), "cadmium.war");
    } finally {
      if(cloned != null) {
        cloned.close();
      }
    }
    
    assertTrue("Initialize method failed", cloned != null);
    File contentDir = new File(testDir, "content");
    assertTrue("Content dir does not exist", contentDir.exists() && contentDir.isDirectory());
    
    File warDir = new File(contentDir, "cadmium.war");
    assertTrue("War dir does not exist", warDir.exists() && contentDir.isDirectory());
    
    File gitDir = new File(warDir, "git-checkout");
    assertTrue("Git repo not checked out", gitDir.exists() && gitDir.isDirectory());
    
    File dotGitDir = new File(gitDir, ".git");
    assertTrue(".Git dir not created", dotGitDir.exists() && dotGitDir.isDirectory());
    
    File renderedContentDir = new File(warDir, "renderedContent");
    assertTrue("RenderedContent dir not created", renderedContentDir.exists() && renderedContentDir.isDirectory());
    
    File otherdotGitDir = new File(renderedContentDir, ".git");
    assertTrue(".Git dir not deleted", !otherdotGitDir.exists());
    
    
  }
}

