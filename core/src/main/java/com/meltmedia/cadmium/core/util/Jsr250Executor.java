package com.meltmedia.cadmium.core.util;

import java.util.Set;

import org.slf4j.Logger;

/**
 * Executes PostConstruct and PreDestroy methods.
 * @author ctrimble
 *
 */
public interface Jsr250Executor {
  public Set<Object> getInstances();
  
  public void postConstruct();
  
  public void preDestroy();
}
