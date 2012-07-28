package com.meltmedia.cadmium.core.git;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayedGitServiceInitializer implements Closeable {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  protected GitService git;
  protected CountDownLatch latch;
  
  public DelayedGitServiceInitializer() {
    latch = new CountDownLatch(1);
  }
  
  public GitService getGitService() throws Exception {
    try {
      logger.debug("Getting git service.");
      latch.await();
    } catch(InterruptedException e) {
      logger.debug("\n\nInterrupted!\n\n", e);
      throw new Exception("Shutting down!!!");
    }
    return git;
  }
  
  public void setGitService(GitService git) {
    if(git != null) {
      logger.debug("Setting git service");
      this.git = git;
      latch.countDown();
    }
  }

  @Override
  public void close() throws IOException {
    if(git != null) {
      IOUtils.closeQuietly(git);
      git = null;
    }
    latch.notifyAll();
  }
  
}
