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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.reflections.ReflectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * JUnit test case to test the SchedulerTask Runnable wrapper and annotation processing.
 * 
 * @author John McEntire
 *
 */
@RunWith(Parameterized.class)
public class SchedulerTaskTest {
  
  @Parameters
  public static Collection<Object[]> data() {
    props = new Properties();
    props.setProperty("tst", "test");
    return Arrays.asList(new Object[][]{
        {new SchedulerTask(TestTask.class), true, new RunVerifier()},
        {new SchedulerTask(TestTask.class), false, new RunVerifier()},
        {new SchedulerTask(TestTask.class, 
            ReflectionUtils.getAllMethods(TestTask.class, 
                ReflectionUtils.withName("coordinatorOnly")).iterator().next()), true, new CoordinatorOnlyVerifier()},
        {new SchedulerTask(TestTask.class, 
            ReflectionUtils.getAllMethods(TestTask.class, 
                ReflectionUtils.withName("coordinatorOnly")).iterator().next()), false, new CoordinatorOnlyVerifier()},
        {new SchedulerTask(TestTask.class, 
            ReflectionUtils.getAllMethods(TestTask.class, 
                ReflectionUtils.withName("testingArguments")).iterator().next()), true, new TestingArgumentsVerifier()},
        {new SchedulerTask(TestTask.class, 
            ReflectionUtils.getAllMethods(TestTask.class, 
                ReflectionUtils.withName("testingArguments")).iterator().next()), false, new TestingArgumentsVerifier()},
        {new SchedulerTask(TestTask.class, 
            ReflectionUtils.getAllMethods(TestTask.class, 
                ReflectionUtils.withName("testingInterval")).iterator().next()), true, new TestingIntervalVerifier()},
        {new SchedulerTask(TestTask.class, 
            ReflectionUtils.getAllMethods(TestTask.class, 
                ReflectionUtils.withName("testingInterval")).iterator().next()), false, new TestingIntervalVerifier()},
        {new SchedulerTask(TestTask.class, 
            ReflectionUtils.getAllMethods(TestTask.class, 
                ReflectionUtils.withName("testingDelayWithInterval")).iterator().next()), true, new TestingDelayWithIntervalVerifier()},
        {new SchedulerTask(TestTask.class, 
            ReflectionUtils.getAllMethods(TestTask.class, 
                ReflectionUtils.withName("testingDelayWithInterval")).iterator().next()), false, new TestingDelayWithIntervalVerifier()}
    });
  }

  private SchedulerTask scheduler;
  private TestTask task;
  private boolean isCoordinator;
  private Verifier verifier; 
  private static Properties props;
  
  public SchedulerTaskTest(SchedulerTask scheduler, boolean isCoordinator, Verifier verifier) {
    this.scheduler = scheduler;
    this.task = mock(TestTask.class);
    this.isCoordinator = isCoordinator;
    this.verifier = verifier;
    
    final MembershipTracker membershipTracker = mock(MembershipTracker.class);
    ChannelMember member = mock(ChannelMember.class);
    when(member.isMine()).thenReturn(isCoordinator);
    when(membershipTracker.getCoordinator()).thenReturn(member);
    
    final String test = "testing";
    final String notUsed = "not-used";
    
    Injector injector = Guice.createInjector(new AbstractModule(){

      @Override
      protected void configure() {
        bind(String.class).annotatedWith(TestString.class).toInstance(test);
        bind(String.class).annotatedWith(UsedTestString.class).toInstance(notUsed);
        bind(Properties.class).toInstance(props);
        bind(TestTask.class).toInstance(task);
        bind(MembershipTracker.class).toInstance(membershipTracker);
      }
      
    });
    
    injector.injectMembers(scheduler);
  }
  
  @Test
  public void testRun() {
    scheduler.getTask().run();
    verifier.verify(scheduler, task, isCoordinator);
  }
  
  public static interface Verifier {
    public void verify(SchedulerTask scheduler, TestTask task, boolean coordinator);
  }
  
  public static class RunVerifier implements Verifier {

    @Override
    public void verify(SchedulerTask scheduler, TestTask task, boolean coordinator) {
      assertTrue("Delay not -1", scheduler.getDelay() == -1l);
      assertTrue("Interval not -1", scheduler.getInterval() == -1l);
      assertTrue("TimeUnit not Milliseconds", scheduler.getTimeUnit() == TimeUnit.MILLISECONDS);
      assertTrue("Is coordinator only", !scheduler.isCoordinatorOnly());
      Mockito.verify(task, only()).run();
    }
    
  }
  
  public static class CoordinatorOnlyVerifier implements Verifier {

    @Override
    public void verify(SchedulerTask scheduler, TestTask task, boolean coordinator) {
      assertTrue("Delay not -1", scheduler.getDelay() == -1l);
      assertTrue("Interval not -1", scheduler.getInterval() == -1l);
      assertTrue("TimeUnit not Milliseconds", scheduler.getTimeUnit() == TimeUnit.MILLISECONDS);
      assertTrue("Is not coordinator only", scheduler.isCoordinatorOnly());
      if(coordinator) {
        Mockito.verify(task, only()).coordinatorOnly();
      } else {
        Mockito.verifyZeroInteractions(task);
      }
    }
    
  }
  
  public static class TestingArgumentsVerifier implements Verifier {

    @Override
    public void verify(SchedulerTask scheduler, TestTask task, boolean coordinator) {
      assertTrue("Delay not 100", scheduler.getDelay() == 100l);
      assertTrue("Interval not -1", scheduler.getInterval() == -1l);
      assertTrue("TimeUnit not Milliseconds", scheduler.getTimeUnit() == TimeUnit.MILLISECONDS);
      assertTrue("Is coordinator only", !scheduler.isCoordinatorOnly());
      Mockito.verify(task, only()).testingArguments(eq("testing"), eq(props));
    }
    
  }
  
  public static class TestingIntervalVerifier implements Verifier {

    @Override
    public void verify(SchedulerTask scheduler, TestTask task, boolean coordinator) {
      assertTrue("Delay not -1", scheduler.getDelay() == -1l);
      assertTrue("Interval not 1", scheduler.getInterval() == 1l);
      assertTrue("TimeUnit not days", scheduler.getTimeUnit() == TimeUnit.DAYS);
      assertTrue("Is coordinator only", !scheduler.isCoordinatorOnly());
      Mockito.verify(task, only()).testingInterval();
    }
    
  }
  
  public static class TestingDelayWithIntervalVerifier implements Verifier {

    @Override
    public void verify(SchedulerTask scheduler, TestTask task, boolean coordinator) {
      assertTrue("Delay not 10", scheduler.getDelay() == 10l);
      assertTrue("Interval not 5", scheduler.getInterval() == 5l);
      assertTrue("TimeUnit not hours", scheduler.getTimeUnit() == TimeUnit.HOURS);
      assertTrue("Is coordinator only", !scheduler.isCoordinatorOnly());
      Mockito.verify(task, only()).testingDelayWithInterval();
    }
    
  }
}
