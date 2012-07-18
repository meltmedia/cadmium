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
package com.meltmedia.cadmium.vault.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.meltmedia.cadmium.core.CadmiumModule;
import com.meltmedia.cadmium.core.meta.ConfigProcessor;
import com.meltmedia.cadmium.vault.VaultContentPreprocessor;
import com.meltmedia.cadmium.vault.VaultListener;
import com.meltmedia.cadmium.vault.service.ResourceFetcher;
import com.meltmedia.cadmium.vault.service.VaultConstants;
import com.meltmedia.cadmium.vault.service.VaultService;

@CadmiumModule
public class VaultModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(String.class).annotatedWith(Names.named(VaultConstants.BASE_URL)).toInstance("http://vault.meltmedia.com/");
    bind(Integer.class).annotatedWith(Names.named(VaultConstants.WATCH_INTERVAL_SECONDS)).toInstance(300);
    bind(String.class).annotatedWith(Names.named(VaultConstants.PROPERTIES_FILE_NAME)).toInstance("vault.properties");
    
    bind(ResourceFetcher.class);
    
    bind(VaultService.class);

    bind(VaultListener.class).to(VaultContentPreprocessor.class);
    
    bind(ConfigProcessor.class).annotatedWith(Names.named(VaultConstants.VAULT_CONFIG_PROCESSOR_NAME)).to(VaultContentPreprocessor.class).asEagerSingleton();
  }

}
