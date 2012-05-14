package com.meltmedia.cadmium.core.commands;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;

public class DummyCommandAction implements CommandAction {
  
  private boolean failTest = false;
  private boolean throwException = false;
  
  public DummyCommandAction(boolean failTest, boolean throwException) {
    this.failTest = failTest;
    this.throwException = throwException;
  }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    if(throwException) {
      throw new Exception("");
    }
    return failTest;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {
    throw new Error("I failed at "+ctx.getMessage().getCommand());
  }

}
