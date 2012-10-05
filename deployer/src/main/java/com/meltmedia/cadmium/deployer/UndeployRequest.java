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
package com.meltmedia.cadmium.deployer;

import com.meltmedia.cadmium.core.commands.AbstractMessageBody;

public class UndeployRequest extends AbstractMessageBody {

  private String domain;
  private String context;

  public String getDomain() {
    return domain;
  }

  public String getContext() {
    return context;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setContext(String context) {
    this.context = context;
  }

}
