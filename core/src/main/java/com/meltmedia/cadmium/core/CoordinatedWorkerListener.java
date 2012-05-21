package com.meltmedia.cadmium.core;

public interface CoordinatedWorkerListener {
  public void workDone();
  public void workFailed();
}
