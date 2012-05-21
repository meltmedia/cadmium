package com.meltmedia.cadmium.core;

import org.jgroups.Address;

import com.meltmedia.cadmium.core.messaging.Message;

public class CommandContext {
  private Address source;
  private Message message;
  
  public CommandContext(Address source, Message message) {
    this.source = source;
    this.message = message;
  }

  public Address getSource() {
    return source;
  }

  public Message getMessage() {
    return message;
  }
}
