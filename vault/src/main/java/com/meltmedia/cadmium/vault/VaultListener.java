package com.meltmedia.cadmium.vault;

public interface VaultListener {
  public void safetyUpdated(String guids[]);
  public void safetyUpdateFailed();
  public void safetyMissing();
}
