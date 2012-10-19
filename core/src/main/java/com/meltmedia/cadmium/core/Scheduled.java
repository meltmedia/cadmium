package com.meltmedia.cadmium.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Classes and methods with this annotation will be scheduled to run in the scheduler. 
 * 
 * @author John McEntire
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scheduled {
  
  /**
   * The delay to wait before the first run of this task. A value less than or equal 
   * to 0 will cause this task to be run immediately.
   */
  long delay() default -1l;
  
  /**
   * The interval to run this task. A value less than or equal 
   * to 0 will cause this task not to be scheduled with an interval.
   */
  long interval() default -1l;
  
  /**
   * The time unit for the interval.
   */
  TimeUnit unit() default TimeUnit.MILLISECONDS;
}
