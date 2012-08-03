package com.meltmedia.cadmium.core.api;

public class BasicApiResponse {
  private String message;
  private String uuid;
  private Long timestamp;
  
  public BasicApiResponse(){ timestamp = System.currentTimeMillis(); }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}
