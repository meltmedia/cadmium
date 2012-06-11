package com.meltmedia.cadmium.cli;

public interface CliCommand {
  public String getCommandName();
  public void execute() throws Exception;
}
