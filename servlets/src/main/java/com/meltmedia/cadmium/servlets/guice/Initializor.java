package com.meltmedia.cadmium.servlets.guice;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.meltmedia.cadmium.core.worker.CheckInitializedTask;
import com.meltmedia.cadmium.core.worker.InitializeTask;

@Singleton
public class Initializor implements Closeable {
  private ExecutorService pool = null;
  
  @Inject
  public Initializor(InitializeTask task, CheckInitializedTask task2) {
    this.pool = Executors.newSingleThreadExecutor();
    
    pool.submit(task2.setExecutor(pool).setFuture(pool.submit(task)));
  }

  @Override
  public void close() throws IOException {
    pool.shutdownNow();
  }
  
}
