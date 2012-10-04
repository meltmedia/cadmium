package com.meltmedia.cadmium.core.commands;

public class StateUpdateRequest extends AbstractMessageBody {

  private String state;
  private String uuid;
  private String configState;

  public String getState() {
    return state;
  }

  public String getUuid() {
    return uuid;
  }

  public String getConfigState() {
    return configState;
  }

  public void setState(String state) {
    this.state = state;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setConfigState(String configState) {
    this.configState = configState;
  }

}
