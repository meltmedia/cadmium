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
