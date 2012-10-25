package com.meltmedia.cadmium.core.util;

import com.google.inject.AbstractModule;

public class AnnotationScanModule
  extends AbstractModule
{

  @Override
  protected void configure() {
    bind(TestServiceInterface.class).to(TestServiceImpl.class);
  }

}
