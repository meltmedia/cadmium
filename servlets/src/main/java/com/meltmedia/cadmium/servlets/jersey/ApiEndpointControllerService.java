package com.meltmedia.cadmium.servlets.jersey;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.ApiEndpointAccessController;
import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.commands.ApiEndpointAccessRequest;
import com.meltmedia.cadmium.core.commands.ApiEndpointAccessRequest.UpdateOpteration;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

/**
 * Jersey service to manage access to CadmiumApiEndpoints.
 * 
 * @author John McEntire
 *
 */
@CadmiumSystemEndpoint
@Path("/api")
public class ApiEndpointControllerService extends AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected ApiEndpointAccessController controller;
  
  @Inject
  protected MessageSender sender;
  
  /**
   * Retrieves a list of already disabled endpoints.
   * 
   * @param auth
   * @return
   * @throws Exception 
   */
  @GET
  @Path("/disable/list")
  @Produces(MediaType.APPLICATION_JSON)
  public String[] get(@HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    return controller.getDisabled();
  }
  
  /**
   * Disables the passed in endpoints. Must not include prefix "/api".
   * 
   * @param paths
   * @param auth
   * @throws Exception 
   */
  @POST
  @Path("/disable")
  @Consumes(MediaType.APPLICATION_JSON)
  public void disable(String paths[], @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    sendJGroupsMessage(UpdateOpteration.DISABLE, paths, auth);
  }
  
  /**
   * Reenables the passed in endpoints. Must not include prefix "/api"
   * @param paths
   * @param auth
   * @throws Exception 
   */
  @POST
  @Path("/enable")
  @Consumes(MediaType.APPLICATION_JSON)
  public void enable(String paths[], @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    sendJGroupsMessage(UpdateOpteration.ENABLE, paths, auth);
  }

  /**
   * Sends jgroups message to make sure that this operation is executed on all nodes in the cluster.
   * 
   * @param operation
   * @param paths
   * @param auth
   * @throws Exception
   */
  private void sendJGroupsMessage(UpdateOpteration operation, String[] paths,
      String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    
    ApiEndpointAccessRequest messageBody = new ApiEndpointAccessRequest();
    messageBody.setEndpoints(paths);
    messageBody.setOperation(operation);
    Message<ApiEndpointAccessRequest> msg = new Message<ApiEndpointAccessRequest>(ProtocolMessage.API_ENDPOINT_ACCESS, messageBody);
    log.debug("Sending jgroups message: "+msg);
    sender.sendMessage(msg, null);
  }
  
}
