package com.meltmedia.cadmium.core;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class LoggerServiceResponse {
  private Map<String, LoggerConfig[]> configs;
  
  public LoggerServiceResponse(){}
  public LoggerServiceResponse(Map<String, LoggerConfig[]> configs) {
    this.configs = configs;
  }

  public Map<String, LoggerConfig[]> getConfigs() {
    return configs;
  }

  public void setConfigs(Map<String, LoggerConfig[]> configs) {
    this.configs = configs;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
