package com.meltmedia.cadmium.cli;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Sets up new dev and meltqa branches with a command given basename.", separators="=")
public class NewBranchCommand {
  
  @Parameter(names="--repo", description="Repository URI", required=true)
  private String repo;
  
  @Parameter(description="\"New branch name\"", required=true)
  private List<String> basename;
  
  public void execute() throws Exception {
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
