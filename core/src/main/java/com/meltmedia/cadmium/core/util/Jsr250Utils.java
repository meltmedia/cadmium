package com.meltmedia.cadmium.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;

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
      safeInvokeMethod(obj, aMethod);
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
      safeInvokeMethod(obj, aMethod);
    }
  }

  /**
   * Invokes a method safely on a Object regardless of accessibility (Unless not allowed with security settings).
   * 
   * @param obj
   * @param aMethod
   * @throws Exception
   */
  private static void safeInvokeMethod(Object obj, Method aMethod)
      throws Exception {
    boolean accessible = aMethod.isAccessible();
    try {
      if(!accessible) {
        aMethod.setAccessible(true);
      }
      if(Modifier.isStatic(aMethod.getModifiers())) {
        aMethod.invoke(null);
      } else {
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
      methodsToRun.addAll(getMethodsWithAnnotation(clazz, annotation, log));
      clazz = clazz.getSuperclass();
    }
    return methodsToRun;
  }
  
  /**
   * Locates all methods annotated with a given annotation that are declared directly in the class passed in.
   * 
   * @param clazz
   * @param annotation
   * @param log
   * @return
   */
  private static Set<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation, Logger log) {
    Set<Method> annotatedMethods = new TreeSet<Method>();
    Method classMethods[] = clazz.getDeclaredMethods();
    for(Method classMethod : classMethods) {
      if(classMethod.isAnnotationPresent(annotation) && classMethod.getParameterTypes().length == 0) {
        annotatedMethods.add(classMethod);
      }
    }
    return annotatedMethods;
  }
}
