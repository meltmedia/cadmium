package com.meltmedia.cadmium.core.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.meltmedia.cadmium.core.CommandAction;

@Singleton
public class CommandMapProvider implements Provider<Map<String, CommandAction>> {
  
  @Inject
  protected Set<CommandAction> commandSet;
  
  private Map<String, CommandAction> actionMap =  new HashMap<String, CommandAction>();
  
  @Inject
  public CommandMapProvider(Set<CommandAction> commandSet) {
    for( CommandAction command : commandSet ) {
      actionMap.put(command.getName(), command);
    }
    actionMap = Collections.unmodifiableMap(actionMap);
  }

  @Override
  public Map<String, CommandAction> get() {
    return actionMap;
  }

}
