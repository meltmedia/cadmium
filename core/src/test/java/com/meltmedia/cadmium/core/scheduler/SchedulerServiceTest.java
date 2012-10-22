package com.meltmedia.cadmium.core.scheduler;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.meltmedia.cadmium.core.Scheduled;

import static org.mockito.Mockito.*;

/**
 * JUnit test case to test the SchedulerService scheduling and Guice binding. 
 * 
 * @author John McEntire
 *
 */
public class SchedulerServiceTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testBindScheduled() {
    Binder binder = mock(Binder.class);
    AnnotatedBindingBuilder<Set<SchedulerTask>> builder = mock(AnnotatedBindingBuilder.class);
    when(binder.bind(new TypeLiteral<Set<SchedulerTask>>(){})).thenReturn(builder);
    
    Reflections reflections = new Reflections("com.meltmedia.cadmium.core.scheduler", 
        new TypeAnnotationsScanner(), 
        new SubTypesScanner(),
        new MethodAnnotationsScanner());
    
    SchedulerService.bindScheduled(binder, reflections);
    
    verify(binder, times(5)).requestInjection(isNotNull());
    verify(builder, times(1)).toInstance((Set<SchedulerTask>) isNotNull());
  }
  
  @Test
  public void testSetupScheduler() {
    ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);
    Set<SchedulerTask> tasks = new HashSet<SchedulerTask>();
    tasks.add(new SchedulerTask(TestTask.class));
    Set<Method> methods = ReflectionUtils.getAllMethods(TestTask.class,
          ReflectionUtils.withAnnotation(Scheduled.class));
    
    for(Method method: methods) {
      tasks.add(new SchedulerTask(TestTask.class, method));
    }
    
    SchedulerService service = new SchedulerService();
    service.executor = executor;
    service.tasks = tasks;
    
    service.setupScheduler();
    
    verify(executor, times(2)).execute((Runnable) isNotNull());
    verify(executor, times(1)).schedule((Runnable) isNotNull(), eq(100l), eq(TimeUnit.MILLISECONDS));
    verify(executor, times(1)).scheduleWithFixedDelay((Runnable) isNotNull(), eq(0l), eq(1l), eq(TimeUnit.DAYS));
    verify(executor, times(1)).scheduleWithFixedDelay((Runnable) isNotNull(), eq(10l), eq(5l), eq(TimeUnit.HOURS));
  }
}
