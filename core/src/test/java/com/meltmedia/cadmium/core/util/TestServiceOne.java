package com.meltmedia.cadmium.core.util;

import javax.annotation.PostConstruct;

public class TestServiceOne
  implements TestServiceOneIF
{
  public TestServiceTwoIF serviceTwo;
  
  @PostConstruct
  public void init() {
    
  }
}
