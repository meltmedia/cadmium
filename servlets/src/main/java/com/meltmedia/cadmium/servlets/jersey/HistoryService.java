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

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.commands.CommandResponse;
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
  @Named("HISTORY_RESPONSE")
  private CommandResponse response;
  
  @Inject
  private HistoryManager historyManager;
  
  @Inject
  private MembershipTracker membershipTracker;

  @GET
  @Produces("application/json")
  public String getHistory(@QueryParam("limit") @DefaultValue("-1") int limit, @QueryParam("filter") @DefaultValue("false") boolean filter, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    ChannelMember coordinator = membershipTracker.getCoordinator();
    if(coordinator.isMine()) {
      log.info("Responding with my own history");
      List<HistoryEntry> history = historyManager.getHistory(limit, filter);
      return new Gson().toJson(history, new TypeToken<List<HistoryEntry>>(){}.getType());
    } else {
      log.info("Getting coordinators history");
      response.reset(coordinator);
      Message msg = new Message();
      msg.setCommand(ProtocolMessage.HISTORY_REQUEST);
      msg.getProtocolParameters().put("limit", limit+"");
      msg.getProtocolParameters().put("filter", filter+"");
      
      sender.sendMessage(msg, coordinator);
      
      int timeout = 240;
      while(timeout-- > 0) {
        Thread.sleep(500l);
        Message returnMsg = response.getResponse(coordinator);
        if(returnMsg != null) {
          if(returnMsg.getProtocolParameters().containsKey("history")) {
            return returnMsg.getProtocolParameters().get("history");
          }
          break;
        }
      }
    }
    return "[]";
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
        throw new Exception(entry.getComment());
      }
    }
    return found+"";
  }
}
