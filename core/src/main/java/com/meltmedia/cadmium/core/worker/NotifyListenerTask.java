package com.meltmedia.cadmium.core.worker;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.meltmedia.cadmium.core.CoordinatedWorkerListener;

public class NotifyListenerTask implements Callable<Boolean> {
  
  private CoordinatedWorkerListener listener;
  private Future<Boolean> previousTask;
  
  public NotifyListenerTask(CoordinatedWorkerListener listener, Future<Boolean> previousTask) {
    this.listener = listener;
    this.previousTask = previousTask;
  }

  @Override
  public Boolean call() throws Exception {
    if(previousTask != null) {
      Boolean lastResponse = previousTask.get();
      if(lastResponse != null && !lastResponse.booleanValue() ) {
        listener.workFailed();
        throw new Exception("Previous task failed");
      }
    }
    Thread.sleep(1000l);
    listener.workDone();
    
    return true;
  }

}
