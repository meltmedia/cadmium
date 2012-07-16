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

public class BranchCreator {
  private GitService clonedRemote;
  private File tmpRepoLocal;
  
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
  
  public boolean createBranchForDevAndQa(String basename) throws Exception {
    boolean branchCreated = clonedRemote.newRemoteBranch("cd-dev-"+basename);
    System.out.println("Created branch \"cd-dev-"+basename+"\"");
    branchCreated = branchCreated && clonedRemote.newRemoteBranch("cd-qa-"+basename);
    System.out.println("Created branch \"cd-qa-"+basename+"\"");
    return branchCreated;
  }
  
  public boolean createBranchForGene(String basename) throws Exception {
    boolean branchCreated = clonedRemote.newRemoteBranch("cd-gene-"+basename);
    System.out.println("Created branch \"cd-gene-"+basename+"\"");
    return branchCreated;
  }
  
  public GitService getGitService() {
    return this.clonedRemote;
  }
  
  public void closeAndRemoveLocal() throws Exception {
    try{
      clonedRemote.close();
    } finally {
      FileSystemManager.deleteDeep(tmpRepoLocal.getAbsoluteFile().getAbsolutePath());
    }
  }
}
