package com.meltmedia.cadmium.servlets.guice;

import com.google.inject.PrivateModule;
import com.meltmedia.cadmium.core.CadmiumModule;
import com.meltmedia.cadmium.core.worker.CheckInitializedTask;
import com.meltmedia.cadmium.core.worker.InitializeTask;

@CadmiumModule
public class InitializeModule extends PrivateModule {

  @Override
  protected void configure() {
    bind(InitializeTask.class);
    bind(CheckInitializedTask.class);
    bind(Initializor.class).asEagerSingleton();
    expose(Initializor.class);
  }

}
