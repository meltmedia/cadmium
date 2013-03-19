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
