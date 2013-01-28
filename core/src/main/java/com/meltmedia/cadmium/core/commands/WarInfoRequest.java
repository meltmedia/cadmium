package com.meltmedia.cadmium.core.commands;

import com.meltmedia.cadmium.core.WarInfo;

public class WarInfoRequest extends AbstractMessageBean {
  private WarInfo warInfo;

  public WarInfo getWarInfo() {
    return warInfo;
  }

  public void setWarInfo(WarInfo warInfo) {
    this.warInfo = warInfo;
  }

}
