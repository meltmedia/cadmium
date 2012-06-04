package com.meltmedia.cadmium.core.git;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.FileSystemManager;

public class GitService {
  private static final Logger log = LoggerFactory.getLogger(GitService.class);
  
  protected Repository repository;
  protected Git git;
  
  public GitService(Repository repository) {
    this.repository = repository;
    git = new Git(this.repository);
  }
  
  protected GitService(Git gitRepo) {
    this.git = gitRepo;
    this.repository = gitRepo.getRepository();
  }
  
  public static void setupSsh(String sshDir) {
    SshSessionFactory.setInstance(new GithubConfigSessionFactory(sshDir));
  }
  
  public static GitService createGitService(String repositoryDirectory) throws Exception {
    if(repositoryDirectory != null) {
      if(!repositoryDirectory.endsWith(".git")) {
        String gitDir = FileSystemManager.getChildDirectoryIfExists(repositoryDirectory, ".git");
        if(gitDir != null) {
          repositoryDirectory = gitDir;
        } else {
          repositoryDirectory = null;
        }
      }
      if(repositoryDirectory != null 
          && FileSystemManager.isDirector(repositoryDirectory) 
          && FileSystemManager.canRead(repositoryDirectory)
          && FileSystemManager.canWrite(repositoryDirectory)){
        return new GitService(new FileRepository(repositoryDirectory));        
      }
    }
    throw new Exception("Invalid git repo");
  }
  
  public static GitService cloneRepo(String uri, String dir) throws Exception {
    if(dir == null || !FileSystemManager.exists(dir)) {
      log.debug("Cloning \""+uri+"\" to \""+dir+"\"");
      CloneCommand clone = Git.cloneRepository();
      clone.setCloneAllBranches(false);
      clone.setCloneSubmodules(false);
      if(dir != null) {
        clone.setDirectory(new File(dir));
      }
      clone.setURI(uri);
      
      return new GitService(clone.call());
    } 
    return null;
  }
  
  public static String moveContentToBranch(String source, GitService gitRepo, String targetBranch, String comment) throws Exception {
    if(!gitRepo.getBranchName().equals(targetBranch)) {
      File tmpDir = File.createTempFile("git_", targetBranch);
      if(tmpDir.delete()){
        log.info("Cloning repository @ " + gitRepo.getBaseDirectory() + " for branch " + targetBranch);
        GitService forBranch = GitService.cloneRepo(gitRepo.getBaseDirectory(), tmpDir.getAbsolutePath());
        String returnVal = null;
        try { 
          forBranch.switchBranch(targetBranch);
          forBranch.pull();
          
          returnVal = moveContentToGit(source, forBranch, comment);
          log.info("Pushing branch modifications back to repository @ " + gitRepo.getBaseDirectory());
          forBranch.git.push().call();
        } finally {
          if(forBranch != null) {
            forBranch.close();
          }
        }
        return returnVal;
      } else {
        throw new Exception("Failed to delete temp file for temp dir creation.");
      }
    } else if(!new File(gitRepo.getBaseDirectory()).getAbsoluteFile().getAbsolutePath().equals(new File(source).getAbsoluteFile().getAbsolutePath())){
      return moveContentToGit(source, gitRepo, comment);
    } else {
      throw new Exception("Source must not be the same as the target.");
    }
  }
  
  private static String moveContentToGit(String source, GitService git, String comment) throws Exception {
    return git.checkinNewContent(source, comment);
  }
  
  public static GitService initializeContentDirectory(String uri, String branch, String root, String warName) throws Exception {
    if(!FileSystemManager.exists(root)) {
      log.info("Content Root directory [{}] does not exist. Creating!!!", root);
      if(!new File(root).mkdirs()) {
        throw new Exception("Failed to create cadmium root @ "+root);
      }
    }
    String warDir = FileSystemManager.getChildDirectoryIfExists(root, warName);
    if(warDir == null) {
      log.info("War directory [{}] does not exist. Creating!!!", warName);
      if(!new File(root, warName).mkdirs()) {
        throw new Exception("Failed to create war content directory @ " + warDir);
      }
    }
    warDir = FileSystemManager.getChildDirectoryIfExists(root, warName);
    GitService cloned = null;
    if(FileSystemManager.getChildDirectoryIfExists(warDir, "git-checkout") == null) {
      log.info("Cloning remote git repository to git-checkout");
      cloned = cloneRepo(uri, new File(warDir, "git-checkout").getAbsolutePath());
      
      if(!cloned.checkForRemoteBranch(branch)) {
        String envString = System.getProperty("com.meltmedia.cadmium.environment", "dev");
        branch = "cd-"+envString+"-"+branch;
      }
      log.info("Switching to branch {}", branch);
      cloned.switchBranch(branch);
      
    } else {
      cloned = createGitService(FileSystemManager.getChildDirectoryIfExists(warDir, "git-checkout"));
    }

    if(cloned == null) {
      throw new Exception("Failed to clone remote github repo from "+uri);
    }
    
    String dirList[] = FileSystemManager.getDirectoriesInDirectory(warDir, "renderedContent");
    if(dirList.length == 0) {
      log.info("RenderedContent directory does not exist. Creating!!!");
      GitService rendered = null;
      try {
        rendered = GitService.cloneRepo(new File(FileSystemManager.getChildDirectoryIfExists(warDir, "git-checkout"), ".git").getAbsolutePath(), new File(warDir, "renderedContent").getAbsolutePath());

        log.info("Removing .git directory from freshly cloned renderedContent directory.");
        String gitDir = FileSystemManager.getChildDirectoryIfExists(new File(warDir, "renderedContent").getAbsolutePath(), ".git");
        if(gitDir != null) {
          FileSystemManager.deleteDeep(gitDir);
        }
        
      } finally {
        if(rendered != null) {
          rendered.close();
        }
      }
    }
    
    return cloned;
  }
  
  public static GitService init(String site, String dir) throws Exception {
  	String repoPath = dir + "/" + site;
  	log.debug("Repository Path :" + repoPath);
  	Repository repo = new FileRepository(repoPath + "/.git");
  	try {
  		repo.create();
  		Git git = new Git(repo);
  		
  		File localGitRepo = new File(repoPath);
  		localGitRepo.mkdirs();
  		new File(localGitRepo, "delete.me").createNewFile();
  		
  		git.add().addFilepattern("delete.me").call();
  		git.commit().setMessage("initial commit").call();
  		return new GitService(git);  		
  	} catch (IllegalStateException e) {
  		System.out.println("Repo Already exists locally");
  	}
		return null;
  }
  
  public String getRepositoryDirectory() throws Exception {
    return this.repository.getDirectory().getAbsolutePath();
  }
  
  public String getBaseDirectory() throws Exception {
    return FileSystemManager.getParent(getRepositoryDirectory());
  }
  
  public boolean pull() throws Exception {
    log.debug("Pulling latest updates from remote git repo");
    return git.pull().call().isSuccessful();
  }
  
  public void switchBranch(String branchName) throws Exception {
    if(branchName != null && !repository.getBranch().equals(branchName)) {
      log.info("Switching branch from {} to {}", repository.getBranch(), branchName);
      CheckoutCommand checkout = git.checkout();
      checkout.setName(branchName);
      if(this.repository.getRef(branchName) == null) {
        CreateBranchCommand create = git.branchCreate();
        create.setName(branchName);
        create.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
        create.setStartPoint("origin/"+branchName);
        create.call();
      }
      checkout.call();
    }
  }
  
  public void resetToRev(String revision) throws Exception {
    if(revision != null) {
      log.info("Resetting to sha {}", revision);
      git.reset().setMode(ResetType.HARD).setRef(revision).call();
    }
  }
  
  private boolean checkForRemoteBranch(String branchName) throws Exception {
    log.info("Getting list of existing branches.");
    List<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();
    boolean branchExists = false; 
    if(refs != null) {
      for(Ref ref : refs) {
        if(ref.getName().endsWith("/" + branchName)) {
          branchExists = true;
          break;
        }
      }
    }
    return branchExists;
  }
  
  public boolean newRemoteBranch(String branchName) throws Exception {
    try{
      log.info("Purging branches that no longer have remotes.");
      git.fetch().setRemoveDeletedRefs(true).call();
    } catch(Exception e) {
      log.warn("Tried to fetch from remote when there is no remote.");
      return false;
    }
    boolean branchExists = checkForRemoteBranch(branchName);
    if(!branchExists) {
      Ref ref = git.branchCreate().setName(branchName).call();
      git.push().add(ref).call();
      return true;
    } else {
      log.info(branchName + " already exists.");
    }
    return false;
  }
  
  public String checkinNewContent(String sourceDirectory, String message) throws Exception {
    log.info("Purging old content.");
    RmCommand remove = git.rm();
    for(String filename : new File(getBaseDirectory()).list()) {
      if(!filename.equals(".git")) {
        remove.addFilepattern(filename);
      }
    }
    remove.call();
    log.info("Committing removal of content.");
    git.commit().setMessage("Removed old content for deployment \""+message+"\"").call();
    log.info("Copying in new content.");
    FileSystemManager.copyAllContent(sourceDirectory, getBaseDirectory(), true);
    log.info("Adding new content.");
    AddCommand add = git.add();
    for(String filename : new File(getBaseDirectory()).list()) {
      if(!filename.equals(".git")) {
        add.addFilepattern(filename);
      }
    }
    add.call();
    log.info("Committing new content.");
    git.commit().setMessage(message).call();
    return getCurrentRevision();
  }
  
  public String getBranchName() throws Exception {
    return repository.getBranch();
  }
  
  public String getCurrentRevision() throws Exception {
    return repository.getRef(getBranchName()).getObjectId().getName();
  }
  
  public void close() throws Exception {
    this.repository.close();
  }
}
