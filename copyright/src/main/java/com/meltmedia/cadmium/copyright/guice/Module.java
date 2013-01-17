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
package com.meltmedia.cadmium.copyright.guice;

import com.google.inject.AbstractModule;
import com.meltmedia.cadmium.copyright.service.CopyrightConfigProcessor;
import com.meltmedia.cadmium.copyright.service.CopyrightResourceHandler;
import com.meltmedia.cadmium.copyright.service.ResourceHandler;
import com.meltmedia.cadmium.core.CadmiumModule;

/**
 * Adds the dynamic copyright updating feature to cadmium.
 * 
 * @author John McEntire
 *
 */
@CadmiumModule
public class Module extends AbstractModule {

  @Override
  protected void configure() {
    bind(CopyrightConfigProcessor.class);
    bind(ResourceHandler.class).to(CopyrightResourceHandler.class);
  }

}
