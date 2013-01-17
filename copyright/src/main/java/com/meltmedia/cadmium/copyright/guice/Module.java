package com.meltmedia.cadmium.copyright.guice;

import com.google.inject.AbstractModule;
import com.meltmedia.cadmium.copyright.service.CopyrightConfigProcessor;
import com.meltmedia.cadmium.copyright.service.CopyrightResourceHandler;
import com.meltmedia.cadmium.copyright.service.ResourceHandler;
import com.meltmedia.cadmium.core.CadmiumModule;

/**
 * Adds the dynamic copyright updating feature to cadmium.
 * 
 * @author John McEntire
 *
 */
@CadmiumModule
public class Module extends AbstractModule {

  @Override
  protected void configure() {
    bind(CopyrightConfigProcessor.class);
    bind(ResourceHandler.class).to(CopyrightResourceHandler.class);
  }

}
