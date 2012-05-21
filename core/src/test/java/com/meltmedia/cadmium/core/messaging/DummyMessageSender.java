package com.meltmedia.cadmium.core.messaging;

public class DummyMessageSender implements MessageSender {
  
  public Message msg;
  public ChannelMember dest;

  @Override
  public void sendMessage(Message msg, ChannelMember dest) throws Exception {
    this.msg = msg;
    this.dest = dest;
  }

}
