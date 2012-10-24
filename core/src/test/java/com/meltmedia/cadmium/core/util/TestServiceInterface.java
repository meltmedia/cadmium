package com.meltmedia.cadmium.core.util;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public interface TestServiceInterface {
  
  @PostConstruct
  public void doConstruct();
  
  @PreDestroy
  public void doDestroy();
}
