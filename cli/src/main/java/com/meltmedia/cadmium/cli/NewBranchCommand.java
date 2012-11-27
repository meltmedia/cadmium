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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Creates branches in git.
 * 
 * 
 * @author John McEntire
 * @author Brian Barr
 *
 */
@Parameters(commandDescription = "Sets up new branches with a command given basename.", separators="=")
public class NewBranchCommand implements CliCommand {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Parameter(names={"--repo","-r"}, description="Repository URI", required=true)
  private String repo;
  
  @Parameter(names={"--empty","-e"}, description="Create empty branches.")
  private boolean empty = false;
  
  @Parameter(description="<newBranchName>", required=true)
  private List<String> basename;
  
  public void execute() throws Exception {
    BranchCreator creator = new BranchCreator(repo);
    try {
      for(String name : basename) {
        creator.createNewBranches(name, empty, log);
      }
    } finally {
      creator.closeAndRemoveLocal();
    }
  }

  @Override
  public String getCommandName() {
    return "branch";
  }

}
