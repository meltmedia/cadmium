package com.meltmedia.cadmium.vault.service;

import java.io.IOException;

import com.meltmedia.cadmium.vault.SafetyMissingException;
import com.meltmedia.cadmium.vault.VaultListener;

public class DummyVaultService extends VaultService {
  
  String resource;
  ResourceFetcher fetcher;
  VaultListener listener;

  public DummyVaultService() {
    super();
  }

  @Override
  public String getSafety(String guid) throws SafetyMissingException,
      IOException {
    return resource;
  }

  @Override
  public void setVaultListener(VaultListener listener) {
    this.listener = (VaultListener)listener;
  }

  @Override
  VaultListener getVaultListener() {
    return listener;
  }

  @Override
  void notifyListener(String[] guids) {
    if(listener != null) {
      listener.safetyUpdated(guids);
    }
  }

  @Override
  ResourceFetcher getFetcher() {
    return fetcher;
  }

  @Override
  public void finalize() {
  }
  
  public void setResource(String resource) {
    this.resource = resource;
  }
  
  

}
