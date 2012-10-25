package com.meltmedia.cadmium.core.util;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.inject.Singleton;

@Singleton
public class TestServiceImpl extends AbstractTestService {
    
  @PostConstruct
  void alreadyConstructed() {
  }
  
  @PreDestroy
  void destroying() {
  }

  @Override
  @PostConstruct
  public void doConstruct() {
    super.doConstruct();
  }

  @Override
  @PreDestroy
  public void doDestroy() {
    super.doDestroy();
  }

}
