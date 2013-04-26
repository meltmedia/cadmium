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

import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.github.ApiClient;

/**
 * A Cadmium cli command that does nothing other then force the {@link CadmiumCli} instance to check and authorized the current user with a GitHub API token.
 * 
 * @author John McEntire
 *
 */
@Parameters(commandDescription="This will just check your Github api token and reauthorize it if needed.", separators="=")
public class CheckGithubTokenCommand extends AbstractAuthorizedOnly implements
    CliCommand {

  /**
   * The command name to invoke this command.  In this case "check".
   */
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
    ApiClient apiClient = new ApiClient(ApiClient.getToken(), false);
    int rateRemain = apiClient.getRateLimitRemain();
    System.out.println("Your Github API token is valid.");
    System.out.println(rateRemain+ " requests remaining.");
  }

}
