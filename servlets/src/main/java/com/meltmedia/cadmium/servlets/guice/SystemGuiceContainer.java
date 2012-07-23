package com.meltmedia.cadmium.servlets.guice;


import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.WebApplication;

@Singleton
public class SystemGuiceContainer extends GuiceContainer {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final long serialVersionUID = -9144790337872193140L;
  
  private Injector injector;

  @Inject
  public SystemGuiceContainer(Injector injector) {
    super(injector);
    this.injector = injector;
  }

  @Override
  protected void initiate(ResourceConfig config, WebApplication webapp) {
    logger.debug("Initiating with Config ["+config+"] and webapp ["+webapp+"]");
    webapp.initiate(config, new GuiceProviderFactory(config, injector, CadmiumSystemEndpoint.class));
  }

}
