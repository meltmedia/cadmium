package com.meltmedia.cadmium.vault.service;

import java.io.IOException;

import com.meltmedia.cadmium.vault.SafetyMissingException;

public class DummyResourceManager extends ResourceManager {

  public DummyResourceManager() {}
  
  protected boolean accessUpdated = false;

  @Override
  public String getSafety(String guid) throws SafetyMissingException,
      IOException {
    return guid;
  }

  @Override
  public void updateAccessTime(String guid) {
    accessUpdated = true;
  }

  @Override
  public void run() {
  }

  @Override
  public String[] updateSafety() throws SafetyMissingException, IOException {
    return new String[]{};
  }

  @Override
  public void persistVaultProperties() {
  }

}
