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

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Creates branches in git.
 * 
 * @deprecated
 * 
 * @author John McEntire
 * @author Brian Barr
 *
 */
@Parameters(commandDescription = "Sets up new dev and meltqa branches with a command given basename.", separators="=")
public class NewBranchCommand implements CliCommand {
  
  @Parameter(names="--repo", description="Repository URI", required=true)
  private String repo;
  
  @Parameter(description="<newBranchName>", required=true)
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

  @Override
  public String getCommandName() {
    return "new-branch";
  }

}
