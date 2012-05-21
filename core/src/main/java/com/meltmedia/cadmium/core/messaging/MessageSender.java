package com.meltmedia.cadmium.core.messaging;

public interface MessageSender {
  public void sendMessage(Message msg, ChannelMember dest) throws Exception;
  public String getGroupName();
}
