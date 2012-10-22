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
