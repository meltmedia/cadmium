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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.ApiEndpointAccessController;
import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class ApiEndpointAccessCommandAction implements
    CommandAction<ApiEndpointAccessRequest> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected ApiEndpointAccessController controller;

  @Override
  public String getName() { return ProtocolMessage.API_ENDPOINT_ACCESS; }

  @Override
  public boolean execute(CommandContext<ApiEndpointAccessRequest> ctx)
      throws Exception {
    ApiEndpointAccessRequest req = ctx.getMessage().getBody();
    if(req != null && req.getEndpoints() != null) {
      if(req.getOperation() == ApiEndpointAccessRequest.UpdateOpteration.DISABLE) {
        for(String endpoint : req.getEndpoints()) {
          log.info("Disabling endpoint /api"+endpoint);
          controller.disable(endpoint);
        }
      } else if(req.getOperation() == ApiEndpointAccessRequest.UpdateOpteration.ENABLE) {
        for(String endpoint : req.getEndpoints()) {
          log.info("Enabling endpoint /api"+endpoint);
          controller.enable(endpoint);
        }
      }
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext<ApiEndpointAccessRequest> ctx,
      Exception e) {}

}
