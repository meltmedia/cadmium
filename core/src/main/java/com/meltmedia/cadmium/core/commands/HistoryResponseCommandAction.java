package com.meltmedia.cadmium.core.commands;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;

@Singleton
public class HistoryResponseCommandAction implements CommandAction, CommandResponse {
  private final Logger log = LoggerFactory.getLogger(getClass()); 
  
  protected Map<ChannelMember, Message> responses = new HashMap<ChannelMember, Message>();

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    log.info("Recevied response for HISTORY_REQUEST from {}", ctx.getSource());
    responses.put(new ChannelMember(ctx.getSource()), ctx.getMessage());
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }

  @Override
  public Message getResponse(ChannelMember member) {
    if(responses.containsKey(member)) {
      return responses.get(member);
    }
    return null;
  }

  @Override
  public void reset(ChannelMember member) {
    if(responses.containsKey(member)) {
      responses.remove(member);
    }
  }

}
