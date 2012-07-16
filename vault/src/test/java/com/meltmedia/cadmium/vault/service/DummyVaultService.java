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
