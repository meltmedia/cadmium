package com.meltmedia.cadmium.core;

public interface CommandAction {
  public boolean execute(CommandContext ctx) throws Exception;
  public void handleFailure(CommandContext ctx, Exception e);
}
