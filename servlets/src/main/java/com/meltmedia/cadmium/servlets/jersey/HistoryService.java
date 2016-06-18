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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.meltmedia.cadmium.servlets.jersey.error.ErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.commands.CommandResponse;
import com.meltmedia.cadmium.core.commands.HistoryRequest;
import com.meltmedia.cadmium.core.commands.HistoryResponse;
import com.meltmedia.cadmium.core.history.HistoryEntry;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@CadmiumSystemEndpoint
@Path("/history")
public class HistoryService extends AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  private MessageSender sender;
  
  @Inject
  private CommandResponse<HistoryResponse> response;
  
  @Inject
  private HistoryManager historyManager;
  
  @Inject
  private MembershipTracker membershipTracker;

  @Context
  UriInfo uriInfo;

  @GET
  @Produces("application/json")
  public List<HistoryEntry> getHistory(@QueryParam("limit") @DefaultValue("-1") int limit, @QueryParam("filter") @DefaultValue("false") boolean filter, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    ChannelMember coordinator = membershipTracker.getCoordinator();
    if(coordinator.isMine()) {
      log.debug("Responding with my own history");
      return historyManager.getHistory(limit, filter);
    } else {
      log.debug("Getting coordinators history");
      response.reset(coordinator);
      HistoryRequest request = new HistoryRequest();
      request.setLimit(limit);
      request.setFilter(filter);
      Message<HistoryRequest> msg = new Message<HistoryRequest>(ProtocolMessage.HISTORY_REQUEST, request);
      
      sender.sendMessage(msg, coordinator);
      
      int timeout = 240;
      while(timeout-- > 0) {
        Thread.sleep(500l);
        Message<HistoryResponse> returnMsg = response.getResponse(coordinator);
        if(returnMsg != null) {
          return returnMsg.getBody().getHistory();
        }
      }
    }
    return new ArrayList<HistoryEntry>();
  }
  
  @GET
  @Path("{uuid}")
  @Produces(MediaType.TEXT_PLAIN)
  public String findUUID(@PathParam("uuid") String uuid) throws Exception {
    return findUUID(uuid, null);
  }
  
  @GET
  @Path("{uuid}/{timestamp}")
  @Produces(MediaType.TEXT_PLAIN)
  public String findUUID(@PathParam("uuid") String uuid, @PathParam("timestamp") Long since) throws Exception {
    boolean found = false;
    HistoryEntry entry = historyManager.getLatestHistoryEntryByUUID(uuid, since != null ? new Date(since) : null);
    if(entry != null && entry.isFinished()) {
      found = true;
      if(entry.isFailed()) {
        ErrorUtil.throwInternalError(entry.getComment(), uriInfo);
      }
    }
    return found+"";
  }
}
