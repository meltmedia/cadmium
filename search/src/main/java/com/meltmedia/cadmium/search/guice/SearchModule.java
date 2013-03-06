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
package com.meltmedia.cadmium.search.guice;

import com.google.inject.AbstractModule;
import com.meltmedia.cadmium.core.CadmiumModule;
import com.meltmedia.cadmium.core.meta.ConfigProcessor;
import com.meltmedia.cadmium.search.IndexSearcherProvider;
import com.meltmedia.cadmium.search.SearchContentPreprocessor;
import com.meltmedia.cadmium.search.SearchContentPreprocessorClass;

@CadmiumModule
public class SearchModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(IndexSearcherProvider.class).to(SearchContentPreprocessor.class);
    bind(ConfigProcessor.class).annotatedWith(SearchContentPreprocessorClass.class).to(SearchContentPreprocessor.class);
  }

}