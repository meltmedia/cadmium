package com.meltmedia.cadmium.cli;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.git.GitService;

@Parameters(commandDescription = "Initializes the content directory for a new war", separators="=")
public class InitializeCommand {

  @Parameter(names="--war", description="The name of the war that will be deployed.", required=true)
  private String war;
  
  @Parameter(names="--repo", description="Remote git repository url.", required=true)
  private String repo;
  
  @Parameter(names="--root", description="The shared root path", required=true)
  private String sharedRoot;
  
  @Parameter(names="--ssh", description="The .ssh directory that contains the ssl keys for git.", required=false)
  private String sshDirectory;
  
  @Parameter(names="--branch", description="The branch to checkout.", required=false)
  private String branch;
  
  @Parameter(names="--revision", description="The revision to reset to.", required=false)
  private String revision;
  
  @Parameter(names="--contentDir", description="The name of the rendered content directory", required=false)
  private String contentDir="renderedContent";
  
  public void execute() throws Exception {
    File sharedRootDir = new File(sharedRoot);
    if((sharedRootDir.exists() || sharedRootDir.mkdirs()) && sharedRootDir.canWrite() && sharedRootDir.canRead()) {
      setupSsh();
      
      File warDir = createWarDirectory(sharedRootDir);
      
      File gitConfigDir = cloneRemoteRepo(warDir);
      
      File contentDirectory = new File(warDir, contentDir);
      
      createRenderedDirectory(gitConfigDir, contentDirectory);
      System.out.println("Finished!");
    } else {
      System.err.println("The root["+sharedRoot+"] directory specified either doesn't exist or cannot be read or written to.");
      System.exit(1);
    }
  }

  private void createRenderedDirectory(File gitConfigDir, File contentDirectory)
      throws Exception {
    if(!contentDirectory.exists()) {
      System.out.println("Creating content directory ["+contentDirectory.getName()+"]...");
      GitService git = GitService.cloneRepo(gitConfigDir.getAbsoluteFile().getAbsolutePath(), contentDirectory.getAbsoluteFile().getAbsolutePath());
      git.close();
      File contentDirGit = new File(contentDirectory, ".git");
      FileSystemManager.deleteDeep(contentDirGit.getAbsoluteFile().getAbsolutePath());
    }
  }

  private File cloneRemoteRepo(File warDir) throws Exception {
    File gitCheckoutDir = new File(warDir, "git-checkout");
    File gitConfigDir = new File(gitCheckoutDir, ".git");
    if(!gitCheckoutDir.exists() || !gitConfigDir.exists()) {
      System.out.println("Cloning remote git repository ["+repo+"] ...");
      if(gitCheckoutDir.exists()) {
        if(gitCheckoutDir.delete()){
          System.err.println("git-checkout directory already exists and cannot be deleted.");
          System.exit(1);
        }
      }
      GitService git = GitService.cloneRepo(repo, gitCheckoutDir.getAbsoluteFile().getAbsolutePath());
      if(branch != null) {
        System.out.println("Switching branch ["+branch+"]...");
        git.switchBranch(branch);
        git.pull();
      }
      if(revision != null) {
        System.out.println("Resetting to revision ["+revision+"]...");
        git.resetToRev(revision);
      }
      git.close();
    }
    return gitConfigDir;
  }

  private File createWarDirectory(File sharedRootDir) {
    File warDir = new File(sharedRootDir, war);
    if(!warDir.exists()) {
      System.out.println("Creating war directory ["+war+"]...");
      if(!warDir.mkdirs()) {
        System.err.println("Failed to create new directory for war["+warDir.getAbsoluteFile().getAbsolutePath()+"] content");
        System.exit(1);
      }
    }
    return warDir;
  }

  private void setupSsh() {
    if(sshDirectory != null && sshDirectory.length() > 0) {
      System.out.println("Setting up ssh config directory ["+sshDirectory+"]...");
      File sshDir = new File(sshDirectory);
      if(sshDir.exists() && sshDir.canRead()) {
        GitService.setupSsh(sshDirectory);
      } else {
        System.err.println("The ssh["+sshDirectory+"] direcotry specified either doesn't exist or cannot be read.");
        System.exit(1);
      }
    }
  }
}
