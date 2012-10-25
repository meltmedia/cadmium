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
