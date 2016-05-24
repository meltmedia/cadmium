/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.servlets.guice;

import com.meltmedia.cadmium.core.worker.CheckConfigInitializedTask;
import com.meltmedia.cadmium.core.worker.CheckInitializedTask;
import com.meltmedia.cadmium.core.worker.ConfigInitializeTask;
import com.meltmedia.cadmium.core.worker.InitializeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class Initializor implements Closeable {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private ExecutorService pool = null;
  
  @Inject
  public Initializor(InitializeTask task, CheckInitializedTask task2, ConfigInitializeTask task3, CheckConfigInitializedTask task4) {
    log.debug("Submitting initialization tasks.");
    this.pool = Executors.newSingleThreadExecutor();
    
    pool.submit(task4.setExecutor(pool).setFuture(pool.submit(task3)));
    
    pool.submit(task2.setExecutor(pool).setFuture(pool.submit(task)));
  }

  @Override
  public void close() throws IOException {
    try {
      pool.shutdownNow();
    } catch (Throwable t) {}
    pool = null;
  }
  
}
