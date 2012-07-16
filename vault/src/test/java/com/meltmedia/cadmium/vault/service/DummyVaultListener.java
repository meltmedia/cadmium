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
