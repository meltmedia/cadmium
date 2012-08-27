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
   * <p>Creates 2 branches with different prefixes and the given basename.</p>
   * <p>The branch name prefixes used are <code>cd-dev-</code> and 
   * <code>cd-qa-</code></p>
   * 
   * @param basename The common basename to use for the branches.
   * @return true if both of the branches where created and pushed successfully onto the remote repository.
   * @throws Exception 
   * 
   * @deprecated
   */
  public boolean createBranchForDevAndQa(String basename) throws Exception {
    boolean branchCreated = clonedRemote.newRemoteBranch("cd-dev-"+basename);
    System.out.println("Created branch \"cd-dev-"+basename+"\"");
    branchCreated = branchCreated && clonedRemote.newRemoteBranch("cd-qa-"+basename);
    System.out.println("Created branch \"cd-qa-"+basename+"\"");
    return branchCreated;
  }
  
  /**
   * <p>Creates a branch with the prefix of <code>cd-gene-</code> and the given basename.</p>
   * 
   * @param basename The basename to user for the branch.
   * @return true if the branch was created and pushed successfully onto the remote repository.
   * @throws Exception
   * 
   * @deprecated
   */
  public boolean createBranchForGene(String basename) throws Exception {
    boolean branchCreated = clonedRemote.newRemoteBranch("cd-gene-"+basename);
    System.out.println("Created branch \"cd-gene-"+basename+"\"");
    return branchCreated;
  }
  
  /**
   * @return The wrapped GitService instance.
   */
  public GitService getGitService() {
    return this.clonedRemote;
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
