package com.meltmedia.cadmium.servlets.jersey;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.commands.CommandResponse;
import com.meltmedia.cadmium.core.history.HistoryEntry;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

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
}
