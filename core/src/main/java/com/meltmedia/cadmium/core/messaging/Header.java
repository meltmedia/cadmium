package com.meltmedia.cadmium.core.messaging;

public class Header {

  private String command;
  private Long requestTime;
  
  public Header() {
    this.command = null;
    this.requestTime = System.currentTimeMillis();
  }
  
  public Header( String command ) {
    this.command = command;
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
  public void setRequestTime(Long requestTime) {
    this.requestTime = requestTime;
  }
}