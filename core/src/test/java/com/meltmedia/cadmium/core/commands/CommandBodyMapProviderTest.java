package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.meltmedia.cadmium.core.CommandAction;

/**
 * Verifies that the CommandBodyMapProvider can properly scan the generics of a command and map command names to a proper message body type.
 * 
 * @author Christian Trimble
 */
@RunWith(Parameterized.class)
public class CommandBodyMapProviderTest {
  
  private static CommandBodyMapProvider provider = null;
  
  @BeforeClass
  public static void setUp() {
    Set<CommandAction<?>> commands = new HashSet<CommandAction<?>>();
    Collection<Object[]> allParams = getParameters();
    for(Object[] params : allParams ) {
      commands.add((CommandAction<?>)params[0]);
    }
    provider = new CommandBodyMapProvider(commands );
  }
  
  @AfterClass
  public static void tearDown() {
    provider = null;
  }
  
  @Parameters
  public static Collection<Object[]> getParameters() {
    return Arrays.asList(new Object[][] {
        {new HistoryRequestCommandAction(), HistoryRequest.class},
        {new ConfigUpdateCommandAction(), ContentUpdateRequest.class},
        {new ConfigUpdateDoneCommandAction(), ContentUpdateRequest.class},
        {new ConfigUpdateFailedCommandAction(), ContentUpdateRequest.class},
        {new CurrentStateCommandAction(), Void.class}
    });
  }

  private CommandAction<?> commandAction;
  private Class<?> bodyType;
  
  public CommandBodyMapProviderTest(CommandAction<?> commandAction, Class<?> bodyType) {
    this.commandAction = commandAction;
    this.bodyType = bodyType;
  }

  @Test
  public void checkMapping() {
    assertEquals("Incorrect body type for command "+commandAction.getClass().getName(), bodyType, provider.get().get(commandAction.getName()));
  }

}
