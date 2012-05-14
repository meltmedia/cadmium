package com.meltmedia.cadmium.core;

import java.util.Map;

public interface CoordinatedWorker {
  public void beginPullUpdates(Map<String, String> properties);
  public void killUpdate();
  public void setListener(CoordinatedWorkerListener listener);
  public CoordinatedWorkerListener getListener();
}
