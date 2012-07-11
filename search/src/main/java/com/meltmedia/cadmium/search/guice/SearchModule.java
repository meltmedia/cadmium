package com.meltmedia.cadmium.search.guice;

import com.google.inject.AbstractModule;
import com.meltmedia.cadmium.core.meta.ConfigProcessor;
import com.meltmedia.cadmium.search.SearchContentPreprocessor;

public class SearchModule extends AbstractModule {

  @Override
  protected void configure() {
    
    
    bind(ConfigProcessor.class).to(SearchContentPreprocessor.class).asEagerSingleton();
  }

}