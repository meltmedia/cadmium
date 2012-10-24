package com.meltmedia.cadmium.core.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.spi.BindingScopingVisitor;


public final class Jsr250Utils {
  public static void postConsrtuct(Object obj, Logger log) throws Exception {
    
  }
  
  public static void preDestroy(Object obj, Logger log) throws Exception {
    
  }
  
  public static List<Object> findAnnotatedObjects(Injector injector, Class<? extends Annotation>... scopeNames) {
    List<Object> objects = new ArrayList<Object>();
    for( Key<?> key : findAnnotatedKeys(injector, scopeNames) ) {
      objects.add(injector.getInstance(key));
    }
    return objects;
  }

  public static List<Key<?>> findAnnotatedKeys(Injector injector, Class<? extends Annotation>... scopeNames) {
    List<Key<?>> keys = new ArrayList<Key<?>>();
    Collection<Binding<?>> bindings = injector.getAllBindings().values();
   
    for( Class<? extends Annotation> scopeName : scopeNames ) {
      Set<Key<?>> scopeKeys = new TreeSet<Key<?>>();
      for( Binding<?> binding : bindings ) {
        if( inScope(binding, scopeName) ) {
          scopeKeys.add(binding.getKey());
        }
      }
      keys.addAll(scopeKeys);
    }
    
    return keys;
  }
  
  public static boolean inScope(Binding<?> binding, final Class<? extends Annotation> scope) {
    return binding.acceptScopingVisitor(new BindingScopingVisitor<Boolean>() {

      @Override
      public Boolean visitEagerSingleton() {
        return scope == Singleton.class || scope == javax.inject.Singleton.class;
      }

      @Override
      public Boolean visitNoScoping() {
        return false;
      }

      @Override
      public Boolean visitScope(Scope guiceScope) {
        return guiceScope == Scopes.SINGLETON && (scope == Singleton.class || scope == javax.inject.Singleton.class);
      }

      @Override
      public Boolean visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
        return scopeAnnotation == scope;
      }
    });
  }
}
