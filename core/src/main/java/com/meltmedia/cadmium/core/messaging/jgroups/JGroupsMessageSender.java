package com.meltmedia.cadmium.core.messaging.jgroups;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jgroups.JChannel;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageConverter;
import com.meltmedia.cadmium.core.messaging.MessageSender;

@Singleton
public class JGroupsMessageSender implements MessageSender {
  
  @Inject
  protected JChannel channel;

  @Override
  public void sendMessage(Message msg, ChannelMember dest) throws Exception {
    if(msg != null) {
      String msgBody = MessageConverter.serialize(msg);
      channel.send(new org.jgroups.Message(dest != null ? dest.getAddress() : null, null, msgBody));
    }
  }
  
  public void setChannel(JChannel channel) {
    this.channel = channel;
  }

}
