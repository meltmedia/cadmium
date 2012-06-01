package com.meltmedia.cadmium.core.commands;

import com.meltmedia.cadmium.core.SiteDownService;

public class DummySiteDownService implements SiteDownService {

  public boolean didStart = false;
  public boolean didStop = false;
  public boolean isOn = false;
  
  @Override
  public void start() {
    didStart = true;
    isOn = true;
  }

  @Override
  public void stop() {
    didStop = true;
    isOn = false;
  }

	@Override
	public boolean isOn() {
		return isOn;
	}

}
