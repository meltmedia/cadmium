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
package com.meltmedia.cadmium.core.messaging;

/**
 * A message sender for testing.  This class should be replaced with Mockito.
 * 
 * @author John McEntire
 *
 * @param <B1>
 * @param <B2>
 */
public class DummyMessageSender<B1, B2> implements MessageSender {
  
  public Message<B1> msg;
  public ChannelMember dest;
  public Message<B2> msg2;
  public ChannelMember dest2;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void sendMessage(Message msg, ChannelMember dest) throws Exception {
    if(this.msg == null) {
      this.msg = msg;
      this.dest = dest;
    } else {
      this.msg2 = msg;
      this.dest2 = dest;
    }
  }

  @Override
  public String getGroupName() {
    return "";
  }
  
  public void clear() {
    msg = null;
    dest = null;
    msg2 = null;
    dest2 = null;
  }

}
