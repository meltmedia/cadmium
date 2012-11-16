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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
 * @author Christian Trimble
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
      List<Method> newMethods = getMethodsWithAnnotation(clazz, annotation, log);
      for(Method newMethod : newMethods) {
        if(containsMethod(newMethod, methodsToRun)) {
          removeMethodByName(newMethod, methodsToRun);
        } else {
          methodsToRun.add(newMethod);
        }
      }
      clazz = clazz.getSuperclass();
      if(clazz != null && clazz.equals(Object.class)) {
        clazz = null;
      }
    }
    return methodsToRun;
  }
  
  /**
   * Checks if the passed in method already exists in the list of methods. Checks for equality by the name of the method.
   * @param method
   * @param methods
   * @return
   */
  private static boolean containsMethod(Method method, List<Method> methods) {
    if(methods != null) {
      for(Method aMethod : methods){
        if(method.getName().equals(aMethod.getName())) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Removes a method from the given list and adds it to the end of the list.
   * @param method
   * @param methods
   */
  private static void removeMethodByName(Method method, List<Method> methods) {
    if(methods != null) {
      Iterator<Method> itr = methods.iterator();
      Method aMethod = null;
      while(itr.hasNext()) {
        aMethod = itr.next();
        if(aMethod.getName().equals(method.getName())) {
          itr.remove();
          break;
        }
      }
      if(aMethod != null) {
        methods.add(aMethod);
      }
    }
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
    Method classMethods[] = clazz.getDeclaredMethods();
    for(Method classMethod : classMethods) {
      if(classMethod.isAnnotationPresent(annotation) && classMethod.getParameterTypes().length == 0) {
        if(!containsMethod(classMethod, annotatedMethods)) {
          annotatedMethods.add(classMethod);
        }
      }
    }
    Collections.sort(annotatedMethods, new Comparator<Method> () {

      @Override
      public int compare(Method method1, Method method2) {
        return method1.getName().compareTo(method2.getName());
      }
    });
    return annotatedMethods;
  }
  
  /**
   * Creates a Jsr250Executor for the specified scopes.
   * 
   * @param injector
   * @param log
   * @param scopes
   * @return
   */
  public static Jsr250Executor createJsr250Executor(Injector injector, final Logger log, Scope... scopes ) {
    final Set<Object> instances = findInstancesInScopes(injector, scopes);
    final List<Object> reverseInstances = new ArrayList<Object>(instances);
    Collections.reverse(reverseInstances);
    return new Jsr250Executor() {

      @Override
      public Set<Object> getInstances() {
        return instances;
      }

      @Override
      public void postConstruct() {
        for( Object instance : instances ) {
          postConstructQuietly(instance, log);
        }
      }

      @Override
      public void preDestroy() {
        for( Object instance : reverseInstances ) {
          preDestroyQuietly(instance, log);
        }
      }
      
    };
  }
  
  /**
   * Finds all of the instances in the specified scopes.
   * 
   * <p>
   * Returns a list of the objects from the specified scope, sorted by their class name.
   * </p>
   * 
   * @param injector the injector to search.
   * @param scopeAnnotations the scopes to search for.
   * @return the objects bound in the injector.
   */
  public static Set<Object> findInstancesInScopes(Injector injector, Class<? extends Annotation>... scopeAnnotations) {
    Set<Object> objects = new TreeSet<Object>(new Comparator<Object>() {

      @Override
      public int compare(Object o0, Object o1) {
        return o0.getClass().getName().compareTo(o1.getClass().getName());
      }
      
    });
    for( Map.Entry<Key<?>, Binding<?>> entry : findBindingsInScope(injector, scopeAnnotations).entrySet()) {
      Object object = injector.getInstance(entry.getKey());
      objects.add(object);
    }
    return objects;
  }
  
  /**
   * Finds all of the instances in the specified scopes.
   * 
   * <p>
   * Returns a list of the objects from the specified scope, sorted by their class name.
   * </p>
   * 
   * @param injector the injector to search.
   * @param scopeAnnotations the scopes to search for.
   * @return the objects bound in the injector.
   */
  public static Set<Object> findInstancesInScopes(Injector injector, Scope... scopes) {
    Set<Object> objects = new TreeSet<Object>(new Comparator<Object>() {

      @Override
      public int compare(Object o0, Object o1) {
        return o0.getClass().getName().compareTo(o1.getClass().getName());
      }
      
    });
    for( Map.Entry<Key<?>, Binding<?>> entry : findBindingsInScope(injector, scopes).entrySet()) {
      Object object = injector.getInstance(entry.getKey());
      objects.add(object);
    }
    return objects;
  }

  /**
   * Finds all of the unique providers in the injector in the specified scopes.
   * 
   * <p>
   * </p>
   * 
   * @param injector the injector to search.
   * @param scopeAnnotations the scopes to search for.
   * @return the keys for the specified scope names.
   */
  public static Map<Key<?>, Binding<?>> findBindingsInScope(Injector injector, Class<? extends Annotation>... scopeAnnotations) {
    Map<Key<?>,Binding<?>> bindings = new LinkedHashMap<Key<?>, Binding<?>>();
    ALL_BINDINGS: for( Map.Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet() ) {
      for( Class<? extends Annotation> scopeAnnotation : scopeAnnotations ) {
        if( inScope(injector, entry.getValue(), scopeAnnotation )) {
          bindings.put(entry.getKey(), entry.getValue());
          continue ALL_BINDINGS;
        }
      }
    }
    return bindings;
  }
  
  /**
   * Returns the bindings in the specified scope.
   * 
   * @param injector
   * @param scopes
   * @return
   */
  public static Map<Key<?>, Binding<?>> findBindingsInScope(Injector injector, Scope... scopes) {
    Map<Key<?>,Binding<?>> bindings = new LinkedHashMap<Key<?>, Binding<?>>();
    ALL_BINDINGS: for( Map.Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet() ) {
      for( Scope scope : scopes ) {
        if( inScope(injector, entry.getValue(), scope )) {
          bindings.put(entry.getKey(), entry.getValue());
          continue ALL_BINDINGS;
        }
      }
    }
    return bindings;
  }
    
  /**
   * Returns true if the binding is in the specified scope, false otherwise.
   * @param binding the binding to inspect
   * @param scope the scope to look for
   * @return true if the binding is in the specified scope, false otherwise.
   */
  public static boolean inScope(final Injector injector, final Binding<?> binding, final Class<? extends Annotation> scope) {
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
        return injector.getScopeBindings().get(scope) == guiceScope;
      }

      @Override
      public Boolean visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
        return scopeAnnotation == scope;
      }
    });
  }
  
  /**
   * Returns true of the binding is in the scope.
   * 
   * @param injector
   * @param binding
   * @param scope
   * @return
   */
  public static boolean inScope(final Injector injector, final Binding<?> binding, final Scope scope) {
    return binding.acceptScopingVisitor(new BindingScopingVisitor<Boolean>() {

      @Override
      public Boolean visitEagerSingleton() {
        return scope == Scopes.SINGLETON;
      }

      @Override
      public Boolean visitNoScoping() {
        return false;
      }

      @Override
      public Boolean visitScope(Scope guiceScope) {
        return guiceScope == scope;
      }

      @Override
      public Boolean visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
        return injector.getScopeBindings().get(scopeAnnotation) == scope;
      }
    });
    
  }
}
