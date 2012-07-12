package com.meltmedia.cadmium.core.meta;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.Inject;

@Singleton
public class MetaConfigProvider implements Provider<List<ConfigProcessor>> {

  @Inject
  protected RedirectConfigProcessor redirect;
  
  @Inject
  protected MimeTypeConfigProcessor mimeType;
  
  @Inject
  protected SslRedirectConfigProcessor sslRedirect;
  
  @Inject(optional=true)
  @Named("other.config.processor")
  protected ConfigProcessor otherConfig;
  
  @Inject(optional=true)
  @Named("search.processor")
  protected ConfigProcessor searchConfig;
  
  private List<ConfigProcessor> processors = null;
  
  @Override
  public List<ConfigProcessor> get() {
    if(processors == null) {
      processors = new ArrayList<ConfigProcessor>();
      processors.add(redirect);
      processors.add(mimeType);
      processors.add(sslRedirect);
      
      if(otherConfig != null) {
        processors.add(otherConfig);
      }
      
      if(searchConfig != null) {
        processors.add(searchConfig);
      }
    }
    return processors;
  }

}
