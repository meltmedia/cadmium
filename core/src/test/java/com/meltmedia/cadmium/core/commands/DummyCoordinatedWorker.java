package com.meltmedia.cadmium.core.commands;

import java.util.Map;

import com.meltmedia.cadmium.core.CoordinatedWorker;
import com.meltmedia.cadmium.core.CoordinatedWorkerListener;

public class DummyCoordinatedWorker implements CoordinatedWorker {
  
  public boolean updating = false;
  public boolean killed = false;
  public CoordinatedWorkerListener listener;

  @Override
  public void beginPullUpdates(Map<String, String> properties) {
    updating = true;
    if(listener != null) {
      listener.workDone();
    }
  }

  @Override
  public void killUpdate() {
    killed = true;
  }

  @Override
  public void setListener(CoordinatedWorkerListener listener) {
    this.listener = listener;
  }

  @Override
  public CoordinatedWorkerListener getListener() {
    return listener;
  }

}
