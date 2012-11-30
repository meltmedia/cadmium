package com.meltmedia.cadmium.core;

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Holds the name a current logging level for a configured Logger.
 * 
 * @author John McEntire
 *
 */
public class LoggerConfig {
  private String name;
  private String level;
  
  public LoggerConfig(String name, String level) {
    this.name = name;
    this.level = level;
  }
  
  public LoggerConfig(){}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
