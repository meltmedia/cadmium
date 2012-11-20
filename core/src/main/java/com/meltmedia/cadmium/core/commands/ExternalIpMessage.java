package com.meltmedia.cadmium.core.commands;

public class ExternalIpMessage extends AbstractMessageBean {
  private String ip;

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }
}
