package com.meltmedia.cadmium.cli;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Displays usage information for a command.")
public class HelpCommand {
	@Parameter(description = "<subcommand>", arity=1)
	public List<String> subCommand;
}
