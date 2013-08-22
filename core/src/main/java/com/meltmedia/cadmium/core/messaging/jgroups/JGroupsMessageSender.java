/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.messaging.jgroups;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageConverter;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import org.jgroups.JChannel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;

@Singleton
public class JGroupsMessageSender implements MessageSender, Closeable {
  
  @Inject
  protected JChannel channel;
  
  @Inject
  protected MessageConverter messageConverter;

  @Override
  public <B> void sendMessage(Message<B> msg, ChannelMember dest) throws Exception {
    if( msg == null ) return;
    
    org.jgroups.Message message = messageConverter.toJGroupsMessage(msg);
    if( dest != null ) message.setDest(dest.getAddress());
    channel.send(message);
  }
  
  public String getGroupName() {
    return channel.getClusterName();
  }
  
  public void setChannel(JChannel channel) {
    this.channel = channel;
  }

  public void setConverter(MessageConverter messageConverter) {
    this.messageConverter = messageConverter;
  }

  @Override
  public void close() throws IOException {
    channel = null;
  }
}
