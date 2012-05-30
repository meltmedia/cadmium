package com.meltmedia.cadmium.core.commands;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;

public interface CommandResponse {
  public Message getResponse(ChannelMember member);
  public void reset(ChannelMember member);
}
