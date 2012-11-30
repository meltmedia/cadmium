package com.meltmedia.cadmium.core.commands;

public class LoggerConfigRequest extends AbstractMessageBean {
  private String loggerName;
  private String level;

  public String getLoggerName() {
    return loggerName;
  }

  public void setLoggerName(String loggerName) {
    this.loggerName = loggerName;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }
  
}
