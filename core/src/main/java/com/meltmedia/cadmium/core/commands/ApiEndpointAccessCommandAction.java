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
