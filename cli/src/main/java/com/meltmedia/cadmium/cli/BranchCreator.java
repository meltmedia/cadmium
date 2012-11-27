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
package com.meltmedia.cadmium.cli;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.git.GitService;

/**
 * Supplies helper methods to help in git repository management.
 * 
 * @author John McEntire
 * @author Chris Haley
 *
 */
public class BranchCreator {
  private GitService clonedRemote;
  private File tmpRepoLocal;
  
  /**
   * Creates a temporary directory with a cloned remote git repository in it.
   * 
   * @param repoUri The git repository uri.
   * @throws Exception Thrown if the clone fails.
   */
  public BranchCreator(String repoUri) throws Exception {
    tmpRepoLocal = File.createTempFile("tmpClone", ".git");
    if(tmpRepoLocal.delete()) {
      clonedRemote = GitService.cloneRepo(repoUri, tmpRepoLocal.getAbsoluteFile().getAbsolutePath());
    } else {
      System.err.println("ERROR: Failed to delete tmp file.");
    }
    
    if(clonedRemote == null) {
      throw new Exception("Failed to clone remote git repository @ " + repoUri);
    }
  }
  
  /**
   * @return The wrapped GitService instance.
   */
  public GitService getGitService() {
    return this.clonedRemote;
  }
  
  /**
   * Creates required cadmium branches using the given basename. 
   * This will also create the source branch. The branches created will be as follows:
   * <ul>
   *  <li><i>basename</i></li>
   *  <li>cd-<i>basename</i></li>
   *  <li>cfg-<i>basename</i></li>
   * </ul>
   * 
   * @param basename The basename used for all branches that will be created.
   * @param empty If true the branches will all be created with no content. If false the 
   * branches will be created off of the associated <i>master</i> branch.
   * @param log
   * @throws Exception 
   */
  public void createNewBranches(String basename, boolean empty, Logger log) throws Exception {
    createNewBranch(null, basename, empty, log);
    createNewBranch("cd", basename, empty, log);
    createNewBranch("cfg", basename, empty, log);
  }
  
  /**
   * Creates a new branch with the name formatted as <i>prefix</i>-<i>basename</i> 
   * if prefix is null then just the basename is used. If empty is true then the 
   * new branch will be empty otherwise it will created from a branch with the same 
   * formatted name but a basename of master.
   * 
   * @param prefix
   * @param basename
   * @param empty
   * @param log
   * @throws Exception 
   */
  private void createNewBranch(String prefix, String basename, boolean empty, Logger log) throws Exception {
    String currentBranch = clonedRemote.getBranchName();
    String newBranchName = (StringUtils.isNotBlank(prefix)?prefix.trim()+"-":"")+basename.trim();
    String oldBranchName = (StringUtils.isNotBlank(prefix)?prefix.trim()+"-":"")+currentBranch;
    try {
      if(!empty) {
        log.info("Switching to {}", oldBranchName);
        clonedRemote.switchBranch(oldBranchName);
        log.info("Creating new branch {}", newBranchName);
        clonedRemote.newRemoteBranch(newBranchName);
      } else {
        log.info("Creating new empty branch {}", newBranchName);
        clonedRemote.newEmtpyRemoteBranch(newBranchName);
      }
    } finally {
      if(!empty) {
        log.info("Switching back to {}", currentBranch);
        clonedRemote.switchBranch(currentBranch);
      }
    }
  }
  
  /**
   * Closes the wrapped GitService instance and removes the directory and all of the contents of the locally cloned repository.
   * 
   * @throws Exception
   */
  public void closeAndRemoveLocal() throws Exception {
    try{
      clonedRemote.close();
    } finally {
      FileSystemManager.deleteDeep(tmpRepoLocal.getAbsoluteFile().getAbsolutePath());
    }
  }
}
