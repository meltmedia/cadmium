package com.meltmedia.cadmium.jgit.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;


import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.jgroups.CoordinatedWorker;
import com.meltmedia.cadmium.jgroups.CoordinatedWorkerListener;

public class CoordinatedWorkerImpl implements CoordinatedWorker {
  private static final Pattern FNAME_PATTERN = Pattern.compile("^(.+)_(\\d+)$", Pattern.CASE_INSENSITIVE);
	private final Logger log = LoggerFactory.getLogger(getClass());
	public static final String RENDERED_DIRECTORY = "RenderedDirectory";
	
	@Inject
	@Named(RENDERED_DIRECTORY)
	protected String lastUpdatedDir = "";
	private CoordinatedWorkerListener listener;
	private boolean kill = false;
	private boolean running = false;
	
	@Override
	public void beginPullUpdates(final Map<String, String> properties) {
		
		if(!running) {
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					running = true;
					try {
					  File repoDir = new File(properties.get("repo"));
					  if(repoDir.exists() && repoDir.isDirectory()) {
					    File gitDir = new File(repoDir, ".git");
					    if(gitDir.exists() && gitDir.isDirectory()) {
    						Repository repo = new FileRepository(gitDir.getAbsolutePath());
    						Git git = new Git(repo);
    						git.pull().call();
    						if(!kill) {
    						  File lastDir = new File(lastUpdatedDir);
    						  String newDir = lastUpdatedDir;
    						  if(lastDir.exists()) {
    						    File parentDir = lastDir.getParentFile();
    						    String dirName = lastDir.getName();
    						    if(parentDir.exists() && parentDir.canWrite()) {
    						      int nextNum = 0;
    						      Matcher fnameMatcher = FNAME_PATTERN.matcher(dirName);
    						      if(fnameMatcher.matches()) {
    						        nextNum = Integer.parseInt(fnameMatcher.group(2));
    						        dirName = fnameMatcher.group(1);
    						      }
                      nextNum++;
    						      dirName += "_" + nextNum;
    						          						      
    						      File newDirFile = new File(parentDir, dirName);
    						      if(newDirFile.exists()) {
    						        deleteDeep(newDirFile);
    						      }
    						      newDir = newDirFile.getAbsolutePath();
    						    } else {
    						      newDir = null;
    						    }
    						  }
    						  if(newDir != null) {
    						    CloneCommand clone = Git.cloneRepository();
    						    clone.setCloneAllBranches(false);
    						    clone.setCloneSubmodules(false);
    						    clone.setDirectory(new File(newDir));
    						    clone.setURI(gitDir.getAbsolutePath());
    						    
    						    clone.call();
    						    if(!kill) {
      						    lastUpdatedDir = newDir;
      						    
      						    File repoFile = new File(lastUpdatedDir, ".git");
      						    if(repoFile.exists()) {
      						      deleteDeep(repoFile);
       						    }
  
      						    if(!kill) {
      						      listener.workDone(lastUpdatedDir);
      						    }
    						    }
    						  } else if(!kill){
    						    throw new Exception("Cannot create a new directory");
    						  }
    						}
					    } else if(!kill){
					      throw new Exception("Not a valid git repo");
					    }
					  } else if(!kill){
					    throw new Exception("Repo dir ["+properties.get("repo")+"] does not exist or is not a directory");
					  }
					} catch (Exception e) {
						if(!kill) {
							listener.workFailed();
						}
						log.warn("Failed to update git directory", e);
					}
					finally {
						running = false;
					}
				}

        
			}).start();
			
		}
		
	}
	
	private static void deleteDeep(File repoFile) {
    List<File> dirChildren = new ArrayList<File>();
    dirChildren.addAll(Arrays.asList(repoFile.listFiles()));
    if(!dirChildren.isEmpty()) {
      for(int i=0; i<dirChildren.size() ; i++) {
        File file = dirChildren.get(i);
        if(file.isDirectory()) {
          dirChildren.addAll(i+1, Arrays.asList(file.listFiles()));
        } else {
          file.delete();
        }
      }
      for(int i = dirChildren.size()-1; i >= 0; i--) {
        File file = dirChildren.get(i);
        if(file.isDirectory()) {
          file.delete();
        }
      }
    }
    repoFile.delete();
  }

	@Override
	public void killUpdate() {
		kill = true;
		
	}

	@Override
	public void setListener(CoordinatedWorkerListener listener) {
		this.listener = listener;
		
	}

}
