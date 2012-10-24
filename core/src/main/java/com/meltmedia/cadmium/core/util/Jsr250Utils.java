package com.meltmedia.cadmium.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.spi.BindingScopingVisitor;

/**
 * Utilities to facilitate the @PostConstruct and @PreDestroy annotations from jsr250.
 * 
 * @author John McEntire
 *
 */
public final class Jsr250Utils {
  /**
   * Calls all @PostConstruct methods on the object passed in called in order from super class to child class.
   * 
   * @param obj The instance to inspect for Annotated methods and call them.
   * @param log 
   * @throws Exception
   */
  public static void postConstruct(Object obj, Logger log) throws Exception {
    List<Method> methodsToRun = getAnnotatedMethodsFromChildToParent(obj.getClass(), PostConstruct.class, log);
    Collections.reverse(methodsToRun);
    for(Method aMethod : methodsToRun) {
      safeInvokeMethod(obj, aMethod, log);
    }
  }
  
  /**
   * Calls postConstruct with the same arguments, logging any exceptions that are thrown at the level warn.
   */
  public static void postConstructQuietly(Object obj, Logger log) {
    try {
      postConstruct(obj, log);
    }
    catch( Throwable t ) {
      log.warn("Could not @PostConstruct object", t);
    }
  }

  /**
   * Calls all @PreDestroy methods on the object passed in called in order from child class to super class.
   * 
   * @param obj The instance to inspect for Annotated methods and call them.
   * @param log 
   * @throws Exception
   */
  public static void preDestroy(Object obj, Logger log) throws Exception {
    List<Method> methodsToRun = getAnnotatedMethodsFromChildToParent(obj.getClass(), PreDestroy.class, log);
    for(Method aMethod : methodsToRun) {
      safeInvokeMethod(obj, aMethod, log);
    }
  }
  
  /**
   * Calls preDestroy with the same arguments, logging any exceptions that are thrown at the level warn.
   * 
   * @param obj
   * @param log
   */
  public static void preDestroyQuietly(Object obj, Logger log) {
    try {
      preDestroy(obj, log);
    }
    catch( Throwable t ) {
      log.warn("Could not @PreDestroy object", t);
    }
  }

  /**
   * Invokes a method safely on a Object regardless of accessibility (Unless not allowed with security settings).
   * 
   * @param obj
   * @param aMethod
   * @param log
   * @throws Exception
   */
  private static void safeInvokeMethod(Object obj, Method aMethod, Logger log)
      throws Exception {
    boolean accessible = aMethod.isAccessible();
    try {
      if(!accessible) {
        log.debug("Setting method to accessible.");
        aMethod.setAccessible(true);
      }
      if(Modifier.isStatic(aMethod.getModifiers())) {
        log.debug("Invoking static method: {}", aMethod);
        aMethod.invoke(null);
      } else {
        log.debug("Invoking method {} on object instance {}", aMethod, obj);
        aMethod.invoke(obj);
      }
    } finally {
      if(!accessible) {
        log.debug("Unsetting methods accessibility flag.");
        aMethod.setAccessible(accessible);
      }
    }
  }

  /**
   * Locates all annotated methods on the type passed in sorted as declared from the type to its super class.
   * 
   * @param clazz The type of the class to get methods from.
   * @param annotation The annotation to look for on methods.
   * @param log
   * @return
   */
  private static List<Method> getAnnotatedMethodsFromChildToParent(Class<?> clazz, Class<? extends Annotation> annotation,
      Logger log) {
    List<Method> methodsToRun = new ArrayList<Method>();
    while(clazz != null) {
      methodsToRun.addAll(getMethodsWithAnnotation(clazz, annotation, log));
      clazz = clazz.getSuperclass();
      log.debug("Moving up to superclass {}", clazz);
    }
    return methodsToRun;
  }
  
  /**
   * Locates all methods annotated with a given annotation that are declared directly in the class passed in alphabetical order.
   * 
   * @param clazz
   * @param annotation
   * @param log
   * @return
   */
  private static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation, Logger log) {
    List<Method> annotatedMethods = new ArrayList<Method>();
    log.debug("Getting all methods on class {} annotated with {}", clazz, annotation);
    Method classMethods[] = clazz.getDeclaredMethods();
    for(Method classMethod : classMethods) {
      if(classMethod.isAnnotationPresent(annotation) && classMethod.getParameterTypes().length == 0) {
        log.debug("Adding method {}", classMethod);
        annotatedMethods.add(classMethod);
      }
    }
    Collections.sort(annotatedMethods, new Comparator<Method> () {

      @Override
      public int compare(Method method1, Method method2) {
        return method1.toString().compareTo(method2.toString());
      }
    });
    return annotatedMethods;
  }
  
  /**
   * Finds all of the objects in the specified scopes.
   * 
   * <p>
   * All objects returned by this method are sorted first by the scopes provided and then by the key objects they are bound to.
   * </p>
   * 
   * @param injector the injector to search.
   * @param scopeNames the scopes to search for.
   * @return the objects bound in the injector.
   */
  public static List<Object> findAnnotatedObjects(Injector injector, Class<? extends Annotation>... scopeNames) {
    List<Object> objects = new ArrayList<Object>();
    for( Key<?> key : findAnnotatedKeys(injector, scopeNames) ) {
      objects.add(injector.getInstance(key));
    }
    return objects;
  }

  /**
   * Finds all of the keys in the injector for the specified scopes.
   * 
   * <p>
   * All of the keys returned by this method are first sorted by the order of the scope names and then by
   * the keys.
   * </p>
   * 
   * @param injector the injector to search.
   * @param scopeNames the scopes to search for.
   * @return the keys for the specified scope names.
   */
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
  
  /**
   * Returns true if the binding is in the specified scope, false otherwise.
   * @param binding the binding to inspect
   * @param scope the scope to look for
   * @return true if the binding is in the specified scope, false otherwise.
   */
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
