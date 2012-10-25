package com.meltmedia.cadmium.core.util;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.meltmedia.cadmium.core.CommandAction;

/**
 * Creates testing bindings.
 * @author Christian Trimble
 *
 */
public class ScopeScanSecondModule
  extends AbstractModule
{

  @Override
  protected void configure() {
    Multibinder<MultiboundService> multiboundServiceBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<MultiboundService>(){});
    multiboundServiceBinder.addBinding().to(MultiboundServiceOne.class);
    multiboundServiceBinder.addBinding().to(MultiboundServiceTwo.class);
    multiboundServiceBinder.addBinding().to(MultiboundServiceThree.class).in(Scopes.SINGLETON);
  }

}
