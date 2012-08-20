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
 * An Object used to facilitate the help function of the Cadmium CLI. This class gets wired in explicitly.
 * 
 * @author Christian Trimble
 *
 */
@Parameters(commandDescription = "Displays usage information for a command.")
public class HelpCommand {
  /**
   * A field that will be set with the name of the command that help was requested for.
   */
	@Parameter(description = "<subcommand>", arity=1)
	public List<String> subCommand;
}
