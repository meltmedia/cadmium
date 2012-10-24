package com.meltmedia.cadmium.core.util;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public abstract class AbstractTestService implements TestServiceInterface {
  
  @Override
  @PostConstruct
  public void doConstruct() {
  }

  @Override
  @PreDestroy
  public void doDestroy() {
  }
  
  @PostConstruct
  protected void constructedAbstract() {
  }
  
  @PreDestroy
  protected void destroyedAbstract() {
  }

}
