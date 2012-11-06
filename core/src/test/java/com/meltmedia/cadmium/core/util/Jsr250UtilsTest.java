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
