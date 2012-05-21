package com.meltmedia.cadmium.core.meta;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class MetaConfigProvider implements Provider<List<ConfigProcessor>> {

  @Inject
  protected RedirectConfigProcessor redirect;
  
  @Inject
  protected MimeTypeConfigProcessor mimeType;
  
  @Inject
  protected SslRedirectConfigProcessor sslRedirect;
  
  private List<ConfigProcessor> processors = null;
  
  @Override
  public List<ConfigProcessor> get() {
    if(processors == null) {
      processors = new ArrayList<ConfigProcessor>();
      processors.add(redirect);
      processors.add(mimeType);
      processors.add(sslRedirect);
    }
    return processors;
  }

}
