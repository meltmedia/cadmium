/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
