package com.meltmedia.cadmium.core.commands;

public class MaintenanceRequest extends AbstractMessageBody {

  protected String state;
  protected String comment;
  private String openId;

  public String getState() {
    return this.state;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getOpenId() {
    return this.openId;
  }

  public void setOpenId(String openId) {
    this.openId = openId;
  }

}
