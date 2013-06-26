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
package com.meltmedia.cadmium.core.git;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the switch repository functionality.
 * 
 * @see {@link DelayedGitServiceInitializer}
 * 
 * @author John McEntire
 *
 */
@Ignore
public class DelayedGitServiceInitializerTest {

  /**
   * A fully initialized reference.
   */
  private DelayedGitServiceInitializer gitInit;
  
  /**
   * Initializes the {@link GitService} reference to set into the {@link DelayedDelayedGitServiceInitializer}.
   * 
   * @throws Exception
   */
  @Before
  public void initGitService() throws Exception {
    File testDir = new File("target/delayed-git-init-test");
    FileUtils.deleteQuietly(testDir);
    FileUtils.forceMkdir(testDir);
    gitInit = new DelayedGitServiceInitializer();
    GitService tmpGit = GitService.cloneRepo("git://github.com/meltmedia/cadmium.git", new File(testDir, "git-checkout").getAbsolutePath());
    tmpGit.switchBranch("cd-manual");
    tmpGit.resetToRev("3ddc7c498c940f49a1cc53aa3d98104a4d24eaa4");
    assertNotNull("GitService should not be null.", tmpGit);
    gitInit.setGitService(tmpGit);
  }
  
  /**
   * Test the failure scenario for a successful roll back.
   * 
   * @throws Exception
   */
  @Test
  public void testSwitchRepoBadSwitch() throws Exception {
    GitService oldGit = gitInit.git;
    String dir = oldGit.getBaseDirectory();
    String repo = oldGit.getRemoteRepository();
    String branch = oldGit.getBranchName();
    String revision = oldGit.getCurrentRevision();
    Exception e = null;
    try {
      gitInit.switchRepository("git@github.com:meltmedia/doesnotexist.git");
    } catch(Exception e1) {
      e = e1;
    }
    assertNotNull("No exception thrown", e);
    GitService git = gitInit.git;
    assertEquals("Checkout directory is not the same.", dir, git.getBaseDirectory());
    assertEquals("The repository failed to roll back.", repo, git.getRemoteRepository());
    assertEquals("Branch not rolled back", branch, git.getBranchName());
    assertEquals("Revision not rolled back", revision, git.getCurrentRevision());
  }
  
  /**
   * Tests the success scenario.
   * 
   * @throws Exception
   */
  @Test
  public void testSwitchRepoGoodSwitch() throws Exception {
    GitService oldGit = gitInit.git;
    String dir = oldGit.getBaseDirectory();
    String repo = oldGit.getRemoteRepository();
    String branch = oldGit.getBranchName();
    String revision = oldGit.getCurrentRevision();
    String newRepo = "git://github.com/meltmedia/test-content-repo.git";
    Exception e = null;
    try {
      gitInit.switchRepository(newRepo);
    } catch(Exception e1) {
      e = e1;
    }
    assertNull("An Exception was thrown.", e);
    GitService git = gitInit.git;
    assertEquals("Checkout directory is not the same.", dir, git.getBaseDirectory());
    assertEquals("The repository should have changed.", newRepo, git.getRemoteRepository());
    assertTrue("Repo rolled back.", !repo.equals(git.getRemoteRepository()));
    assertTrue("Branch rolled back.", !branch.equals(git.getBranchName()));
    assertTrue("Revision rolled back.", !revision.equals( git.getCurrentRevision() ));
    
  }
  
  /**
   * Cleans up any used resources.
   * 
   * @throws IOException
   */
  @After
  public void closeGitService() throws IOException {
    if(gitInit != null) {
      gitInit.close();
    }
  }
}
