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


import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.meltmedia.cadmium.core.CadmiumApiEndpoint;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

@Singleton
public class ApiGuiceContainer extends ServletContainer {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private static final long serialVersionUID = -7752374974855049603L;
  
  private Injector injector;
  private WebApplication webapp;

  @Inject
  public ApiGuiceContainer(Injector injector) {
    this.injector = injector;
  }
  
  @Override
  protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
    WebConfig webConfig) throws ServletException {
    return new DefaultResourceConfig();
  }

  @Override
  protected void initiate(ResourceConfig config, WebApplication webapp) {
    logger.debug("Initiating with Config ["+config+"] and webapp ["+webapp+"]");
    this.webapp = webapp;
    webapp.initiate(config, new GuiceProviderFactory(config, injector, CadmiumApiEndpoint.class));
  }
  
  public WebApplication getWebApplication() {
    return webapp;
  }

}
