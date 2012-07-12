package com.meltmedia.cadmium.search.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.meltmedia.cadmium.core.meta.ConfigProcessor;
import com.meltmedia.cadmium.search.IndexSearcherProvider;
import com.meltmedia.cadmium.search.SearchContentPreprocessor;
import com.meltmedia.cadmium.search.SearchService;

public class SearchModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(IndexSearcherProvider.class).to(SearchContentPreprocessor.class);
    bind(ConfigProcessor.class).annotatedWith(Names.named("search.processor")).to(SearchContentPreprocessor.class);
    bind(SearchService.class).asEagerSingleton();
  }

}