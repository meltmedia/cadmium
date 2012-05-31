package com.meltmedia.cadmium.core.messaging;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jgroups.blocks.MessageListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;

@Singleton
public class MessageReceiver extends MessageListenerAdapter {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  @Named("commandMap")
  Map<ProtocolMessage, CommandAction> commandMap; 
  
  @Override
  public void receive(org.jgroups.Message msg) {
    Message message = MessageConverter.deserialize(msg.getObject().toString());
    CommandContext ctx = new CommandContext(msg.getSrc(), message);
    
    if(commandMap.containsKey(message.getCommand())) {
      CommandAction action = commandMap.get(message.getCommand());
      if(action != null) {
        try{
          if(!action.execute(ctx)){
            action.handleFailure(ctx, null);
          }
        } catch(Exception e) {
          action.handleFailure(ctx, e);
          log.error("Command [{}] failed: {}", message.getCommand(), e.getMessage());
        }
      }
    }
  }

}
