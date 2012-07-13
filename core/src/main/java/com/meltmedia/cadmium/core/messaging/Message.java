package com.meltmedia.cadmium.core.messaging;

import java.util.HashMap;
import java.util.Map;

public class Message {
  private ProtocolMessage command;
  private Map<String, String> protocolParameters = new HashMap<String, String>();
  private Long requestTime;
  
  public Message() {
    this.requestTime = System.currentTimeMillis();
  }

  public ProtocolMessage getCommand() {
    return command;
  }

  public void setCommand(ProtocolMessage command) {
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
