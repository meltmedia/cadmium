package com.meltmedia.cadmium.core.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import static org.junit.Assert.assertEquals;

public class ScopeScanTest {
  @Test
  public void testSingletonInjector() {
    Injector injector = Guice.createInjector(new ScopeScanFirstModule(), new ScopeScanSecondModule());
    Set<Object> singletons = Jsr250Utils.findInstancesInScopes(injector, Scopes.SINGLETON);

    assertEquals("Wrong number of TestServiceImpls found.", 1, countInstancesOfType(TestServiceImpl.class, singletons));
  }
  
  /**
   * Makes sure that we find bindings in the singleton scope, no matter how they are bound.
   */
  @Test
  public void testDifferentScopeBindings() {
    Injector injector = Guice.createInjector(new ScopeScanFirstModule(), new ScopeScanSecondModule());
    Set<Object> singletons = Jsr250Utils.findInstancesInScopes(injector, Scopes.SINGLETON);
    
    assertEquals("Wrong number of TestServiceOne found.", 1, countInstancesOfType(TestServiceOne.class, singletons));
    assertEquals("Wrong number of TestServiceTwo found.", 1, countInstancesOfType(TestServiceTwo.class, singletons));
    assertEquals("Wrong number of TestServiceThree found.", 1, countInstancesOfType(TestServiceThree.class, singletons));
  }
  
  /**
   * Verifies that objects created in a multibinding are located.
   */
  @Test
  public void testMultibindings() {
    Injector injector = Guice.createInjector(new ScopeScanFirstModule(), new ScopeScanSecondModule());
    Set<Object> singletons = Jsr250Utils.findInstancesInScopes(injector, Scopes.SINGLETON);
    
    assertEquals("Wrong number of MultiboundServiceOne found.", 1, countInstancesOfType(MultiboundServiceOne.class, singletons));
    assertEquals("Wrong number of MultiboundServiceTwo found.", 0, countInstancesOfType(MultiboundServiceTwo.class, singletons));
    assertEquals("Wrong number of MultiboundServiceThree found.", 1, countInstancesOfType(MultiboundServiceThree.class, singletons));
  }
  
  /**
   * Creates an injector 10 times an makes sure that the scanner finds objects in the same order each time by comparing their classes.
   */
  @Test
  public void testInstanceOrder() {
    Injector firstInjector = Guice.createInjector(new ScopeScanFirstModule(), new ScopeScanSecondModule());
    Set<Object> firstSingletons = Jsr250Utils.findInstancesInScopes(firstInjector, Scopes.SINGLETON);

    for( int i = 0; i < 9; i++ ) {
      Injector nextInjector = Guice.createInjector(new ScopeScanFirstModule(), new ScopeScanSecondModule());
      Set<Object> nextSingletons = Jsr250Utils.findInstancesInScopes(nextInjector, Scopes.SINGLETON);
      
      assertEquals(firstSingletons.size(), nextSingletons.size());
      
      Iterator<Object> nextIterator = nextSingletons.iterator();
      for( Object first : firstSingletons ) {
        first.getClass().equals(nextIterator.next().getClass());
      }
    }
  }
  
  public static int countInstancesOfType( Class<?> type, Collection<?> collection ) {
    int count = 0;
    for( Object object : collection ) {
      if( object.getClass() == type ) count++;
    }
    return count;
  }
}
