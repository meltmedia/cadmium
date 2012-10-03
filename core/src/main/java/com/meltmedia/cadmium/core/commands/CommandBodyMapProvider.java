package com.meltmedia.cadmium.core.commands;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;

public class CommandBodyMapProvider implements Provider<Map<String, Class<?>>> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected Set<CommandAction<?>> commandSet;
  
  private Map<String, Class<?>> bodyTypeMap =  new HashMap<String, Class<?>>();
  
  @Inject
  public CommandBodyMapProvider(Set<CommandAction<?>> commandSet) {
    for( CommandAction<?> command : commandSet ) {
      Map<TypeVariable<?>,Type> genericMap = TypeUtils.getTypeArguments(CommandAction.class, command.getClass());
      Type bodyType = genericMap.get(CommandAction.class.getTypeParameters()[0]);
      if( bodyType instanceof Class<?> ) 
        bodyTypeMap.put(command.getName(), (Class<?>)bodyType);
      else {
        log.error("Could not identify message body type for command {}:{}.", command.getName(), command.getClass().getName());
      }
    }
  }

  @Override
  public Map<String, Class<?>> get() {
    return bodyTypeMap;
  }
}
