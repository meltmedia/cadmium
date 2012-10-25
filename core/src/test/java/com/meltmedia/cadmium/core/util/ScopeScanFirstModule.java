package com.meltmedia.cadmium.core.util;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ScopeScanFirstModule
  extends AbstractModule
{

  @Override
  protected void configure() {
    bind(TestServiceInterface.class).to(TestServiceImpl.class);
    bind(TestServiceOneIF.class).to(TestServiceOne.class).in(Singleton.class);
    bind(TestServiceTwoIF.class).to(TestServiceTwo.class);
    bind(TestServiceThreeIF.class).to(TestServiceThree.class);
  }

}
