package com.meltmedia.cadmium.core.commands;

import com.meltmedia.cadmium.core.LoggerConfig;

public class LoggerConfigResponse extends AbstractMessageBean {
  private LoggerConfig loggers[];

  public LoggerConfig[] getLoggers() {
    return loggers;
  }

  public void setLoggers(LoggerConfig[] loggers) {
    this.loggers = loggers;
  }
}
