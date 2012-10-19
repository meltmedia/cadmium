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
