package com.meltmedia.cadmium.cli;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.git.GitService;

@Parameters(commandDescription = "Sets up new dev and meltqa branches with a command given basename.", separators="=")
public class NewBranchCommand {
  
  @Parameter(names="--repo", description="Repository URI", required=true)
  private String repo;
  
  @Parameter(description="\"New branch name\"", required=true)
  private List<String> basename;
  
  public void execute() throws Exception {
    File sshDir = new File(System.getProperty("user.home"), ".ssh");
    if(sshDir.exists()) {
      GitService.setupSsh(sshDir.getAbsolutePath());
    }
    BranchCreator creator = new BranchCreator(repo);
    try {
      for(String name : basename) {
        creator.createBranchForDevAndQa(name);
      }
    } finally {
      creator.closeAndRemoveLocal();
    }
  }

}
