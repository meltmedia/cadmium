package com.meltmedia.cadmium.jgroups;

public interface CoordinatedWorkerListener {
  public void workDone(String newDir);
  public void workFailed();
}
