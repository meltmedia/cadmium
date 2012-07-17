package com.meltmedia.cadmium.core.api;

public class MaintenanceRequest {
  public static enum State { ON, OFF };
  private State state;
  private String comment;
  
  public MaintenanceRequest(){}

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
