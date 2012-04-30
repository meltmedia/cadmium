package com.meltmedia.cadmium.servlets;

public interface ActivationHandler {
  public void activated(ActivatableService service);
  public void deactivated(ActivatableService service);
}
