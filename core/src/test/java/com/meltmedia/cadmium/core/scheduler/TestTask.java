package com.meltmedia.cadmium.core.scheduler;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import com.meltmedia.cadmium.core.CoordinatorOnly;
import com.meltmedia.cadmium.core.Scheduled;

/**
 * Class to test scheduler.
 * 
 * @author John McEntire
 *
 */
@Scheduled
public class TestTask implements Runnable {

  @Override
  public void run() {
    
  }
  
  @Scheduled
  @CoordinatorOnly
  public void coordinatorOnly() {
    
  }
  
  @Scheduled(delay=100l, unit=TimeUnit.MILLISECONDS)
  public void testingArguments(@Named("str") String str, Properties props) {
    
  }
  
  @Scheduled(interval=1l, unit=TimeUnit.DAYS)
  public void testingInterval() {
    
  }
  
  @Scheduled(delay=10l, interval=5l, unit=TimeUnit.HOURS)
  public void testingDelayWithInterval() {
    
  }
}
