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
package com.meltmedia.cadmium.servlets.jersey;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
@Path("/disabled")
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
  @Produces(MediaType.APPLICATION_JSON)
  public String[] get(@HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    return controller.getDisabled();
  }
  
  /**
   * Disables the passed in endpoint. Must not include prefix "/api/".
   * 
   * @param path
   * @param auth
   * @throws Exception 
   */
  @PUT
  @Path("/{path: .*}")
  public void disable(@PathParam("path") String path, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    sendJGroupsMessage(UpdateOpteration.DISABLE, path, auth);
  }
  
  /**
   * Reenables the passed in endpoint. Must not include prefix "/api/"
   * @param paths
   * @param auth
   * @throws Exception 
   */
  @DELETE
  @Path("/{path: .*}")
  public void enable(@PathParam("path") String path, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    sendJGroupsMessage(UpdateOpteration.ENABLE, path, auth);
  }

  /**
   * Sends jgroups message to make sure that this operation is executed on all nodes in the cluster.
   * 
   * @param operation
   * @param paths
   * @param auth
   * @throws Exception
   */
  private void sendJGroupsMessage(UpdateOpteration operation, String path,
      String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    log.debug("Sending jGroups message with operation {} and path {}", operation, path);
    ApiEndpointAccessRequest messageBody = new ApiEndpointAccessRequest();
    messageBody.setEndpoint(path);
    messageBody.setOperation(operation);
    Message<ApiEndpointAccessRequest> msg = new Message<ApiEndpointAccessRequest>(ProtocolMessage.API_ENDPOINT_ACCESS, messageBody);
    log.debug("Sending jgroups message: "+msg);
    sender.sendMessage(msg, null);
  }
  
}
