package com.meltmedia.cadmium.core.commands;

import java.util.HashMap;
import java.util.Map;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;

public abstract class AbstractCommandResponse<T> implements CommandResponse<T> {
  
  protected Map<ChannelMember, Message<T>> responses = new HashMap<ChannelMember, Message<T>>();

  @Override
  public Message<T> getResponse(ChannelMember member) {
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
