package com.meltmedia.cadmium.core.util;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import static org.junit.Assert.assertEquals;


public class ScopeAnnotationScanTest {
  @Test
  public void testSingletonInjector() {
    Injector injector = Guice.createInjector(new AnnotationScanModule());
    List<Object> singletons = Jsr250Utils.findAnnotatedObjects(injector, Singleton.class);

    assertEquals("Wrong number of TestServiceImpls found.", 1, countInstancesOfType(TestServiceImpl.class, singletons));
  }
  
  public static int countInstancesOfType( Class<?> type, Collection<?> collection ) {
    int count = 0;
    for( Object object : collection ) {
      if( object.getClass() == type ) count++;
    }
    return count;
  }
}
