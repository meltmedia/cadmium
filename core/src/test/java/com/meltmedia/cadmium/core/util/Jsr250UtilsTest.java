package com.meltmedia.cadmium.core.util;

import org.junit.Test;
import org.mockito.InOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

/**
 * Tests for the Jsr250Utils class.
 * 
 * @author John McEntire
 *
 */
public class Jsr250UtilsTest {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Test
  public void testPostConstruct() throws Exception {
    TestServiceImpl testService = mock(TestServiceImpl.class);
    Jsr250Utils.postConstruct(testService, log);
    
    InOrder order = inOrder(testService);
    
    order.verify(testService, times(1)).doConstruct();
    order.verify(testService, times(1)).constructedAbstract();
    order.verify(testService, times(1)).alreadyConstructed();
    order.verifyNoMoreInteractions();
  }
  
  @Test
  public void testPreDestory() throws Exception {
    TestServiceImpl testService = mock(TestServiceImpl.class);
    Jsr250Utils.preDestroy(testService, log);
    
    InOrder order = inOrder(testService);
    
    order.verify(testService, times(1)).destroying();
    order.verify(testService, times(1)).destroyedAbstract();
    order.verify(testService, times(1)).doDestroy();
    order.verifyNoMoreInteractions();
  }
}
