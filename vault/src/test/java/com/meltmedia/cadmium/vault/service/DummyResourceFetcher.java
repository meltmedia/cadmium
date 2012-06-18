package com.meltmedia.cadmium.vault.service;

import java.io.IOException;
import java.util.Date;

import com.meltmedia.cadmium.vault.SafetyMissingException;
import com.meltmedia.cadmium.vault.service.ResourceFetcher;

public class DummyResourceFetcher extends ResourceFetcher {

  public DummyResourceFetcher(String vaultBaseUrl) {
    super(vaultBaseUrl);
  }
  
  String overrideGuid = null;
  boolean fetched = false;

  @Override
  public String fetch(String resourceGuid, Date lastModified) throws SafetyMissingException, IOException {
    fetched = true;
    return overrideGuid == null ? resourceGuid : overrideGuid;
  }

}
