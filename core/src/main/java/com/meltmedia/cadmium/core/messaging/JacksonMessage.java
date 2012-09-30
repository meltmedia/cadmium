package com.meltmedia.cadmium.core.messaging;

public class JacksonMessage<B> {
  private Header header;
  private B body;
  
  public void setHeader( Header header ) {
    this.header = header;
  }
  
  public Header getHeader() {
    return this.header;
  }
  
  public B getBody() {
    return body;
  }
  
  public void setBody( B body ) {
    this.body = body;
  }
  
  public static class Header {

    private String command;
    private Long requestTime;
    
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
}
