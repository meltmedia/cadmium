package com.meltmedia.cadmium.jgit.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;


import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.jgroups.CoordinatedWorker;
import com.meltmedia.cadmium.jgroups.CoordinatedWorkerListener;
import com.meltmedia.cadmium.jgroups.jersey.UpdateService;
import com.meltmedia.cadmium.jgroups.receivers.UpdateChannelReceiver;

public class CoordinatedWorkerImpl implements CoordinatedWorker {
  private static final Pattern FNAME_PATTERN = Pattern.compile("^(.+)_(\\d+)$", Pattern.CASE_INSENSITIVE);
	private final Logger log = LoggerFactory.getLogger(getClass());
	public static final String RENDERED_DIRECTORY = "RenderedDirectory";
	
	@Inject
	@Named(RENDERED_DIRECTORY)
	protected String lastUpdatedDir = "";
	
	@Inject
	@Named(UpdateService.REPOSITORY_LOCATION)
	protected String repoDirectory = "";
	
	@Inject
	@Named(UpdateChannelReceiver.BASE_PATH)
	protected String baseDirectory = "";
	
	private CoordinatedWorkerListener listener;
	private boolean kill = false;
	private boolean running = false;
	
	@Override
	public void beginPullUpdates(final Map<String, String> properties) {
		if(!running) {

	    
			kill = false;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					running = true;
					FileRepository repo = null;
					try {
					  File repoDir = new File(repoDirectory);
					  if(repoDir.exists() && repoDir.isDirectory()) {
					    File gitDir = new File(repoDir, ".git");
					    if(gitDir.exists() && gitDir.isDirectory()) {
    						repo = new FileRepository(gitDir.getAbsolutePath());
    						Git git = new Git(repo);
    						if(properties.containsKey("branch") && !repo.getBranch().equals(properties.get("branch"))) {
    						  log.info("Switching branch from {} to {}", repo.getBranch(), properties.get("branch"));
                  CreateBranchCommand branch = git.branchCreate().setName(properties.get("branch"));
                  branch.setStartPoint("origin/"+Repository.shortenRefName(properties.get("branch")));
                  branch.setForce(true);
                  branch.call();
                }
    						PullResult result = git.pull().call();
    						if(!kill) {
    						  if(!result.isSuccessful()) {
    						    throw new Exception("Pull failed");
    						  }
  						    if(properties.containsKey("sha")) {
  						      log.info("Resetting to sha {}", properties.get("sha"));
  						      git.reset().setMode(ResetType.HARD).setRef(properties.get("sha")).call();
  						    }
    						  File lastDir = new File(lastUpdatedDir);
    						  String newDir = lastUpdatedDir;
    						  if(lastDir.exists()) {
    						    File parentDir = new File(baseDirectory);
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
      						      Ref ref = repo.getRef(repo.getBranch());
      						      listener.workDone(lastUpdatedDir);
      						      updatePropertiesFile(ref);
      						      cleanUp();
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
					    throw new Exception("Repo dir ["+repoDirectory+"] does not exist or is not a directory");
					  }
					} catch (Exception e) {
						if(!kill) {
							listener.workFailed();
						}
						log.warn("Failed to update git directory", e);
					}
					finally {
						running = false;
						if(repo != null) {
						  repo.close();
						}
					}
				}

        
			}).start();
			
		}
		
	}
	
	private void cleanUp() {
	  new Thread(new Runnable() {

      @Override
      public void run() {
        final File lastUpdated = new File(lastUpdatedDir);
        if(lastUpdated.exists()) {
          File parentDir = lastUpdated.getParentFile();
          Matcher nameMatcher = FNAME_PATTERN.matcher(lastUpdated.getName());
          if(nameMatcher.matches()){
            final String prefixName = nameMatcher.group(1);
            final int dirNumber = Integer.parseInt(nameMatcher.group(2));
            File renderedDirs[] = parentDir.listFiles(new FilenameFilter() {

              @Override
              public boolean accept(File file, String name) {
                if(name.startsWith(prefixName)) {
                  return true;
                }
                return false;
              }
              
            });
            
            for(File renderedDir : renderedDirs) {
              if(renderedDir.getName().equals(prefixName) && dirNumber > 1) {
                try{
                  deleteDeep(renderedDir);
                } catch(Exception e) {
                  log.warn("Failed to delete old dir {}, {}", renderedDir.getName(), e.getMessage());
                }
              } else {
                Matcher otherNameMatcher = FNAME_PATTERN.matcher(renderedDir.getName());
                if(otherNameMatcher.matches()) {
                  int otherDirNumber = Integer.parseInt(otherNameMatcher.group(2));
                  if(otherDirNumber < (dirNumber - 1)) {
                    try{
                      deleteDeep(renderedDir);
                    } catch(Exception e) {
                      log.warn("Failed to delete old dir {}, {}", renderedDir.getName(), e.getMessage());
                    }
                  }
                }
              }
            }
          }
        }
      }
	    
	  }).start();
	}
	
	private void updatePropertiesFile(Ref ref) {
	  Properties configProperties = new Properties();
	  if(new File(baseDirectory, "config.properties").exists()) {
  	  try{
  	    configProperties.load(new FileReader(new File(baseDirectory, "config.properties")));
  	  } catch(Exception e){
  	    log.debug("Failed to load properties file");
  	  }
	  }
	  if(configProperties.containsKey("com.meltmedia.cadmium.lastUpdated")) {
	    configProperties.setProperty("com.meltmedia.cadmium.previous", configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"));
	  }
	  configProperties.setProperty("com.meltmedia.cadmium.lastUpdated", lastUpdatedDir);
    if(configProperties.containsKey("branch")) {
      configProperties.setProperty("branch.last", configProperties.getProperty("branch"));
    }
	  configProperties.setProperty("branch", Repository.shortenRefName(ref.getName()));
    if(configProperties.containsKey("git.ref.sha")) {
      configProperties.setProperty("git.ref.sha.last", configProperties.getProperty("git.ref.sha"));
    }
	  configProperties.setProperty("git.ref.sha", ref.getObjectId().getName());
	  
	  try{
	    configProperties.store(new FileWriter(new File(baseDirectory, "config.properties")), null);
	  } catch(Exception e) {
	    log.warn("Failed to write out config file", e);
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
