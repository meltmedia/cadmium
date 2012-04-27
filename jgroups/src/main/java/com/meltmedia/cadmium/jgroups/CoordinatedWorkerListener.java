package com.meltmedia.cadmium.jgroups;

public interface CoordinatedWorkerListener {
  public void workDone();
  public void workFailed();
}
