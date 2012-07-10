package com.meltmedia.cadmium.search.guice;

import com.google.inject.servlet.ServletModule;
import com.meltmedia.cadmium.core.meta.ConfigProcessor;
import com.meltmedia.cadmium.search.SearchContentPreprocessor;

public class SearchModule extends ServletModule {

  @Override
  protected void configureServlets() {
    super.configureServlets();
    
    bind(ConfigProcessor.class).to(SearchContentPreprocessor.class).asEagerSingleton();
  }

}