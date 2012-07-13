package com.meltmedia.cadmium.core.messaging;

import java.util.HashMap;
import java.util.Map;

public class Message {
  private ProtocolMessage command;
  private Map<String, String> protocolParameters = new HashMap<String, String>();
  private String requestTime;
  
  public Message() {
    this.requestTime = Long.toString(System.currentTimeMillis());
  }

  public ProtocolMessage getCommand() {
    return command;
  }

  public void setCommand(ProtocolMessage command) {
    this.command = command;
  }
  
  public String getRequestTime() {
    return requestTime;
  }
  
  public void setRequestTime( String requestTime ) {
    this.requestTime = requestTime;
  }

  public Map<String, String> getProtocolParameters() {
    return protocolParameters;
  }

  public void setProtocolParameters(Map<String, String> protocolParameters) {
    this.protocolParameters = protocolParameters;
  }
}
