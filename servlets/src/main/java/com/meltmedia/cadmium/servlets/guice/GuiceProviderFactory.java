package com.meltmedia.cadmium.servlets.guice;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Scope;
import com.google.inject.servlet.ServletScopes;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;

public class GuiceProviderFactory extends GuiceComponentProviderFactory {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private Class<? extends Annotation> annotation;

  public GuiceProviderFactory(ResourceConfig config, Injector injector, Class<? extends Annotation> annotation) {
    super(config, injector);
    this.annotation = annotation;
    logger.debug("Filtering out endpoints not annotated with {}", annotation);
    filterClasses(config);
  }

  @Override
  public Map<Scope, ComponentScope> createScopeMap() {
    Map<Scope, ComponentScope> m = super.createScopeMap();

    m.put(ServletScopes.REQUEST, ComponentScope.PerRequest);
    return m;
  }
  
  private void filterClasses(ResourceConfig config) {
    Iterator<Class<?>> clItr = config.getClasses().iterator();
    while(clItr.hasNext()) {
      Class<?> clazz = clItr.next();
      if(ResourceConfig.isRootResourceClass(clazz) && !clazz.isAnnotationPresent(annotation)) {
        logger.debug("Removing endpoint {}", clazz);
        clItr.remove();
      }
    }
  }
}
