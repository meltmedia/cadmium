package com.meltmedia.cadmium.servlets.guice;

import com.meltmedia.cadmium.core.worker.CheckConfigInitializedTask;
import com.meltmedia.cadmium.core.worker.CheckInitializedTask;
import com.meltmedia.cadmium.core.worker.ConfigInitializeTask;
import com.meltmedia.cadmium.core.worker.InitializeTask;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;


/**
 * Test for {@link Initializor}
 */
public class InitializorTest {

  InitializeTask initTask;
  CheckInitializedTask checkInitTask;
  ConfigInitializeTask configInitTask;
  CheckConfigInitializedTask checkConfigInitTask;

  @Before
  public void setUp() {
    initTask = mock(InitializeTask.class);
    checkInitTask = mock(CheckInitializedTask.class);
    configInitTask = mock(ConfigInitializeTask.class);
    checkConfigInitTask = mock(CheckConfigInitializedTask.class);

    when(checkInitTask.setFuture(any(Future.class))).thenReturn(checkInitTask);
    when(checkInitTask.setExecutor(any(ExecutorService.class))).thenReturn(checkInitTask);
    when(checkConfigInitTask.setFuture(any(Future.class))).thenReturn(checkConfigInitTask);
    when(checkConfigInitTask.setExecutor(any(ExecutorService.class))).thenReturn(checkConfigInitTask);

  }

  @Test
  public void shouldExecuteConfigTaskPriorToContent() throws Exception {
    //Given: Tasks that needs to be executed in order
    InOrder inOrder = inOrder(initTask, checkInitTask, configInitTask, checkConfigInitTask);

    //When: I create new initializor and execute tasks
    Initializor initializor = new Initializor(initTask, checkInitTask, configInitTask, checkConfigInitTask);
    initializor.pool.shutdown();
    initializor.pool.awaitTermination(5, TimeUnit.SECONDS);

    //Then: Tasks are executed in order: initTask, checkInitTask, configInitTask, checkConfigInitTask
    inOrder.verify(configInitTask).call();
    inOrder.verify(checkConfigInitTask).call();
    inOrder.verify(initTask).call();
    inOrder.verify(checkInitTask).call();

  }

  @Test
  public void shouldExecuteAllTasksEvenWhenOneOfTheTAsksFail() throws Exception {
    //Given: Tasks that needs to be executed in order and failing config task
    InOrder inOrder = inOrder(initTask, checkInitTask, configInitTask, checkConfigInitTask);
    when(configInitTask.call()).thenThrow(new RuntimeException("MockException: Ignore"));

    //When: I create new initializor and execute tasks
    Initializor initializor = new Initializor(initTask, checkInitTask, configInitTask, checkConfigInitTask);
    initializor.pool.shutdown();
    initializor.pool.awaitTermination(5, TimeUnit.SECONDS);

    //Then: Tasks are executed in order: initTask, checkInitTask, configInitTask, checkConfigInitTask
    inOrder.verify(configInitTask).call();
    inOrder.verify(checkConfigInitTask).call();
    inOrder.verify(initTask).call();
    inOrder.verify(checkInitTask).call();

  }

}
