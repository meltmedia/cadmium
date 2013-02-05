package com.meltmedia.cadmium.servlets.jersey;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.servlets.shiro.AuthenticationManagerCommandAction;
import com.meltmedia.cadmium.servlets.shiro.AuthenticationManagerRequest;
import com.meltmedia.cadmium.servlets.shiro.AuthenticationManagerRequest.RequestType;
import com.meltmedia.cadmium.servlets.shiro.PersistablePropertiesRealm;

/**
 * 
 * Authentication management for site access.
 * 
 * @author John McEntire
 *
 */
@CadmiumSystemEndpoint
@Path("/auth")
public class AuthenticationManagerService extends AuthorizationService {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected MessageSender sender;
  
  @Inject(optional=true)
  protected PersistablePropertiesRealm realm;
  
  /**
   * Returns a list of users specific to this site only.
   * 
   * @param auth
   * @return
   * @throws Exception
   */
  @GET
  public Response getConfigurableUsers(@HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    if( realm != null ) {
      List<String> usernames = new ArrayList<String>();
      usernames = realm.listUsers();
      return Response.ok(new Gson().toJson(usernames), MediaType.APPLICATION_JSON).build();
    }
    return Response.status(Status.NOT_FOUND).build();
  }
  
  /**
   * Adds user credentials for access to this site only.
   * 
   * @param username
   * @param auth
   * @param message
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{user}")
  @Consumes(MediaType.TEXT_PLAIN)
  public Response addUser(@PathParam("user") String username, @HeaderParam("Authorization") @DefaultValue("no token") String auth, String message) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    if( realm != null ){
      AuthenticationManagerRequest req = new AuthenticationManagerRequest();
      req.setAccountName(username);
      req.setPassword(message);
      req.setRequestType(RequestType.ADD);
      
      sendMessage(req);
      return Response.created(new URI("/"+username)).build();
    }
    return Response.status(Status.NOT_FOUND).build();
  }
  
  /**
   * Deletes an user from the user acounts specific to this site only.
   * @param username
   * @param auth
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{user}")
  public Response deleteUser(@PathParam("user") String username, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    if( realm != null ){
      AuthenticationManagerRequest req = new AuthenticationManagerRequest();
      req.setAccountName(username);
      req.setRequestType(RequestType.REMOVE);
      
      sendMessage(req);
      return Response.status(Status.GONE).build();
    }
    return Response.status(Status.NOT_FOUND).build();
  }
  
  /**
   * Generically send a JGroups message to the cluster.
   * 
   * @param req
   */
  private void sendMessage(AuthenticationManagerRequest req) {
    Message<AuthenticationManagerRequest> msg = new Message<AuthenticationManagerRequest>(AuthenticationManagerCommandAction.COMMAND_NAME, req);
    try {
      sender.sendMessage(msg, null);
    } catch (Exception e) {
      log.error("Failed to update authentication.", e);
    }
  }
  
}
