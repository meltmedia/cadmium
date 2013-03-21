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

import com.meltmedia.cadmium.core.git.GitService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;

import java.io.File;

/**
 * Manages a git repository for testing.
 */
public class GitBareRepoInitializer {
  private String repoPath;
  private String checkoutPath;
  private Git bareRepo;
  private GitService clonedGit;

  public GitBareRepoInitializer() {}

  public void init(String repoPath, String sourceDir, String sourceConfigDir) throws Exception {
    File repoDir = new File(repoPath);
    if(repoDir.exists()) {
      FileUtils.forceDelete(repoDir);
    }

    File checkoutDir = new File(repoDir.getAbsoluteFile().getParent(), repoDir.getName()+".checkout");
    if(checkoutDir.exists()) {
      FileUtils.forceDelete(checkoutDir);
    }
    checkoutPath = checkoutDir.getAbsolutePath();

    InitCommand init = Git.init();
    init.setBare(true);
    init.setDirectory(repoDir);
    bareRepo = init.call();

    clonedGit = GitService.cloneRepo(repoPath, checkoutPath);
    clonedGit.checkinNewContent(sourceConfigDir, "Initial commit");
    clonedGit.push(false);
    clonedGit.newEmtpyRemoteBranch("cd-master");
    clonedGit.switchBranch("cd-master");
    clonedGit.checkinNewContent(sourceDir, "Initial commit");
    clonedGit.push(false);
    clonedGit.newEmtpyRemoteBranch("cfg-master");
    clonedGit.switchBranch("cfg-master");
    clonedGit.checkinNewContent(sourceConfigDir, "Initial commit");
    clonedGit.push(false);
  }

  public String setupContentUpdate(String updateDir) throws Exception {
    clonedGit.switchBranch("cd-master");
    clonedGit.checkinNewContent(updateDir, "updated content");
    clonedGit.push(false);
    return clonedGit.getCurrentRevision();
  }

  public String setupConfigUpdate(String updateDir) throws Exception {
    clonedGit.switchBranch("cfg-master");
    clonedGit.checkinNewContent(updateDir, "updated config");
    clonedGit.push(false);
    return clonedGit.getCurrentRevision();
  }

  public void close() throws Exception {
    IOUtils.closeQuietly(clonedGit);
    bareRepo.getRepository().close();
  }

  public String getRepo() {
    return clonedGit.getRemoteRepository();
  }
}
