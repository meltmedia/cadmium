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
