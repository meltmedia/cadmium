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

import java.util.Set;

import org.slf4j.Logger;

/**
 * Executes PostConstruct and PreDestroy methods.
 * 
 * @author Christian Trimble
 *
 */
public interface Jsr250Executor {
  /**
   * Returns the set of instances that will have their @PostConstruct and @PreDestroy annotations called.
   * 
   * @return
   */
  public Set<Object> getInstances();
  
  /**
   * Calls @PostConstruct methods on the instances.
   */
  public void postConstruct();
  
  /**
   * Calls @PreDestroy methods on the instances in reverse order.
   */
  public void preDestroy();
}
