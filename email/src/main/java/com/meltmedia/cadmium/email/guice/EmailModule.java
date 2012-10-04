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
package com.meltmedia.cadmium.email.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.meltmedia.cadmium.core.CadmiumModule;
import com.meltmedia.cadmium.core.config.ConfigurationListener;

@CadmiumModule
public class EmailModule extends AbstractModule {
	
	@SuppressWarnings("rawtypes")
  @Override
	protected void configure() {
    bind(com.meltmedia.cadmium.email.internal.EmailServiceImpl.class).asEagerSingleton();
    Multibinder<ConfigurationListener> listenerBinder = Multibinder.newSetBinder(binder(), ConfigurationListener.class);
    listenerBinder.addBinding().to(com.meltmedia.cadmium.email.internal.EmailServiceImpl.class);
    //bind(EmailResource.class).asEagerSingleton();
	}

}
