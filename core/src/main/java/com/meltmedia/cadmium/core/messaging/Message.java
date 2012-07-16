package com.meltmedia.cadmium.core.messaging;

import java.util.HashMap;
import java.util.Map;

public class Message {
  private String command;
  private Map<String, String> protocolParameters = new HashMap<String, String>();
  private Long requestTime;
  
  public Message() {
    this.requestTime = System.currentTimeMillis();
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }
  
  public Long getRequestTime() {
    return requestTime;
  }
  
  public void setRequestTime( Long requestTime ) {
    this.requestTime = requestTime;
  }

  public Map<String, String> getProtocolParameters() {
    return protocolParameters;
  }

  public void setProtocolParameters(Map<String, String> protocolParameters) {
    this.protocolParameters = protocolParameters;
  }
}
