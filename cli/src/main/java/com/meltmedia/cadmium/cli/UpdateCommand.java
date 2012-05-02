package com.meltmedia.cadmium.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Instructs a site to update its content.")
public class UpdateCommand {
	  @Parameter(description="<site>")
	  private String site;
}
