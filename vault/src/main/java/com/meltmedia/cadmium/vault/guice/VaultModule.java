package com.meltmedia.cadmium.vault.guice;

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.meltmedia.cadmium.core.meta.ConfigProcessor;
import com.meltmedia.cadmium.vault.VaultContentPreprocessor;
import com.meltmedia.cadmium.vault.VaultListener;
import com.meltmedia.cadmium.vault.service.ResourceFetcher;
import com.meltmedia.cadmium.vault.service.VaultConstants;
import com.meltmedia.cadmium.vault.service.VaultService;

public class VaultModule extends ServletModule {

  @Override
  protected void configureServlets() {
    super.configureServlets();
    
    bind(String.class).annotatedWith(Names.named(VaultConstants.BASE_URL)).toInstance("http://vault.meltmedia.com/");
    bind(Integer.class).annotatedWith(Names.named(VaultConstants.WATCH_INTERVAL_SECONDS)).toInstance(300);
    bind(String.class).annotatedWith(Names.named(VaultConstants.PROPERTIES_FILE_NAME)).toInstance("vault.properties");
    
    bind(ResourceFetcher.class);
    
    bind(VaultService.class);

    bind(VaultListener.class).to(VaultContentPreprocessor.class);
    
    bind(ConfigProcessor.class).annotatedWith(Names.named(VaultConstants.VAULT_CONFIG_PROCESSOR_NAME)).to(VaultContentPreprocessor.class).asEagerSingleton();
  }

}
