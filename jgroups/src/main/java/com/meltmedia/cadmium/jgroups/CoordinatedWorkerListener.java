package com.meltmedia.cadmium.jgroups;

import java.io.File;

public interface CoordinatedWorkerListener {
  public void workDone(String newDir);
  public void workFailed();
}
