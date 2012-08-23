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
package com.meltmedia.cadmium.core.commands;

import com.meltmedia.cadmium.core.SiteDownService;

public class DummySiteDownService implements SiteDownService {

  public boolean didStart = false;
  public boolean didStop = false;
  public boolean isOn = false;
  
  @Override
  public void start() {
    didStart = true;
    isOn = true;
  }

  @Override
  public void stop() {
    didStop = true;
    isOn = false;
  }

	@Override
	public boolean isOn() {
		return isOn;
	}

}
