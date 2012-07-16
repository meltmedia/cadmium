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
package com.meltmedia.cadmium.core.messaging;

import com.google.gson.Gson;

public final class MessageConverter {
  private MessageConverter(){}
  
  public static String serialize(Message msg) {
    if(msg != null) {
      Gson gson = new Gson();
      return gson.toJson(msg);
    }
    return null;
  }
  
  public static Message deserialize(String msgStr) {
    if(msgStr != null && msgStr.trim().length() > 0) {
      Gson gson = new Gson();
      return gson.fromJson(msgStr, Message.class);
    }
    return null;
  }
}
