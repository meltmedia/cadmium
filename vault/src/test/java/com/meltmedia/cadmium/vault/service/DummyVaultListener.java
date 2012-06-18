package com.meltmedia.cadmium.vault.service;

import com.meltmedia.cadmium.vault.VaultListener;

public class DummyVaultListener implements VaultListener {

  String guidsUpdated[];
  boolean safetyUpdateFailed = false;
  boolean safetyMissing = false;
  
  @Override
  public void safetyUpdated(String[] guids) {
    guidsUpdated = guids;
  }

  @Override
  public void safetyUpdateFailed() {
    safetyUpdateFailed = true;
  }

  @Override
  public void safetyMissing() {
    safetyMissing = true;
  }

}
