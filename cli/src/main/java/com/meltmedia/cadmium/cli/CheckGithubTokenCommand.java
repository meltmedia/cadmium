package com.meltmedia.cadmium.cli;

import com.beust.jcommander.Parameters;

@Parameters(commandDescription="This will just check your Github api token and reauthorize it if needed.", separators="=")
public class CheckGithubTokenCommand extends AbstractAuthorizedOnly implements
    CliCommand {

  @Override
  public String getCommandName() {
    return "check";
  }

  /**
   * This method does nothing.  All of the logic to validate/acquire a github api token is handled in the CadmiumCli class prior to this being called.
   *  
   */
  @Override
  public void execute() throws Exception {
    System.out.println("Your Github API token is valid.");
  }

}
