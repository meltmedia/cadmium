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
package com.meltmedia.cadmium.servlets.guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.PrivateModule;
import com.meltmedia.cadmium.core.CadmiumModule;
import com.meltmedia.cadmium.core.worker.CheckConfigInitializedTask;
import com.meltmedia.cadmium.core.worker.CheckInitializedTask;
import com.meltmedia.cadmium.core.worker.ConfigInitializeTask;
import com.meltmedia.cadmium.core.worker.InitializeTask;

@CadmiumModule
public class InitializeModule extends PrivateModule {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected void configure() {
    log.debug("Initializing content and configuration.");
    bind(InitializeTask.class);
    bind(CheckInitializedTask.class);
    bind(ConfigInitializeTask.class);
    bind(CheckConfigInitializedTask.class);
    bind(Initializor.class).asEagerSingleton();
    expose(Initializor.class);
  }

}
