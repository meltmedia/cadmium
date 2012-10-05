package com.meltmedia.cadmium.persistence;

import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.meltmedia.cadmium.core.CadmiumModule;
import com.meltmedia.cadmium.core.config.ConfigurationListener;

/**
 * Binds all necessary classes into guice.
 * 
 * @author John McEntire
 *
 */
@CadmiumModule
public class PersistenceModule extends AbstractModule {
  public static final String JPA_UNIT_NAME = "com.meltmedia.cadmium.jpa.persistence";

  @SuppressWarnings("rawtypes")
  @Override
  protected void configure() {
    Properties jpaProperties = new Properties();
    bind(Properties.class).annotatedWith(CadmiumJpaProperties.class).toInstance(jpaProperties);
    install(new JpaPersistModule(JPA_UNIT_NAME).properties(jpaProperties));
    
    Multibinder<ConfigurationListener> listenerBinder = Multibinder.newSetBinder(binder(), ConfigurationListener.class);
    listenerBinder.addBinding().to(PersistenceConfigurationListener.class);
  }

}
