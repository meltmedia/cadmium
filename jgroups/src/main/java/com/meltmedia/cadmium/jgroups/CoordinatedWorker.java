package com.meltmedia.cadmium.jgroups;

import java.util.Map;

public interface CoordinatedWorker {
  public void beginPullUpdates(Map<String, String> properties);
  public void killUpdate();
  public void setListener(CoordinatedWorkerListener listener);
}
