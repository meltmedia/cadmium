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
package com.meltmedia.cadmium.core.util;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ScopeScanFirstModule
  extends AbstractModule
{

  @Override
  protected void configure() {
    bind(TestServiceInterface.class).to(TestServiceImpl.class);
    bind(TestServiceOneIF.class).to(TestServiceOne.class).in(Singleton.class);
    bind(TestServiceTwoIF.class).to(TestServiceTwo.class);
    bind(TestServiceThreeIF.class).to(TestServiceThree.class);
  }

}
