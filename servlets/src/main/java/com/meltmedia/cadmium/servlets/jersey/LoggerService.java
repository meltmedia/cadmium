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

import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.ClusterMembers;
import com.meltmedia.cadmium.core.LoggerConfig;
import com.meltmedia.cadmium.core.LoggerServiceResponse;
import com.meltmedia.cadmium.core.commands.CommandResponse;
import com.meltmedia.cadmium.core.commands.LoggerConfigRequest;
import com.meltmedia.cadmium.core.commands.LoggerConfigResponse;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CadmiumSystemEndpoint
@Path("/logger")
public class LoggerService extends AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  private MessageSender sender;
  
  @Inject
  private CommandResponse<LoggerConfigResponse> response;
  
  @Inject
  @ClusterMembers
  protected List<ChannelMember> members;
  
  @GET
  @Path("{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public LoggerServiceResponse getConfiguredLoggers(@PathParam("name") String name, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    log.trace("Sending logger request for logger: {}", name);
    return new LoggerServiceResponse(sendLoggerConfigRequest(name, null));  
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public LoggerServiceResponse getConfiguredLoggers(@HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    log.trace("Sending logger request for all loggers");
    return getConfiguredLoggers(null, auth);  
  }
  
  @POST
  @Path("{name}/{level}")
  @Produces(MediaType.APPLICATION_JSON)
  public LoggerServiceResponse setConfiguredLoggers(@PathParam("name") String name, @PathParam("level") String level, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    log.trace("Sending logger update: {}[{}]", name, level);
    return new LoggerServiceResponse(sendLoggerConfigRequest(name, level));  
  }
  
  @POST
  @Path("{level}")
  @Produces(MediaType.APPLICATION_JSON)
  public LoggerServiceResponse setConfiguredLoggers(@PathParam("level") String level, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    log.trace("Sending logger update ROOT level: [{}]", level);
    return setConfiguredLoggers(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME, level, auth);  
  }

  private Map<String, LoggerConfig[]> sendLoggerConfigRequest(String name, String level) {
    Map<String, LoggerConfig[]> configs = new HashMap<String, LoggerConfig[]>();
    for(ChannelMember member : members) {
      response.reset(member);
      try {
        LoggerConfigRequest req = new LoggerConfigRequest();
        req.setLoggerName(name);
        req.setLevel(level);
        Message<LoggerConfigRequest> msg = new Message<LoggerConfigRequest>(ProtocolMessage.LOGGER_CONFIG_REQUEST, req);
        sender.sendMessage(msg, member);
        
        int timeout = 120;
        while(timeout-- > 0) {
          Thread.sleep(500l);
          Message<LoggerConfigResponse> returnMsg = response.getResponse(member);
          if(returnMsg != null && returnMsg.getBody().getLoggers() != null) {
            log.trace("Member {} has {} configured loggers.", member.getAddress(), returnMsg.getBody().getLoggers().length);
            configs.put(member.getAddress().toString(), returnMsg.getBody().getLoggers());
            break;
          }
        }
      } catch(Exception e) {
        log.warn("Failed to send message to member "+member, e);
      }
    }
    log.trace("Returning logger configs for {} nodes.", configs.size());
    return configs;
  }
  

}
