/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
  
  public String getGroupName() {
    return channel.getClusterName();
  }
  
  public void setChannel(JChannel channel) {
    this.channel = channel;
  }

}
