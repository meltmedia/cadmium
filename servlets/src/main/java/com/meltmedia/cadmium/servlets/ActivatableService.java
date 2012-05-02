package com.meltmedia.cadmium.servlets;

public interface ActivatableService {
  public void activate(ActivationHandler handler);
  public void deactivate(ActivationHandler handler);
}
