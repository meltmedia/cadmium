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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.deployer.DeploymentTracker.DeploymentStatus;

/**
 * CommandAction that triggers other nodes in a cluster to respond with
 * their {@link DeploymentStatus}.
 * 
 * @author John McEntire
 *
 */
public class DeployUpdateCommandAction implements CommandAction {
  
  public static final String DEPLOY_UPDATE_ACTION = "DEPLOY_UPDATE";
  
  @Inject
  protected DeploymentTracker tracker;
  
  @Inject
  protected MembershipTracker memberTracker;
  
  @Inject
  protected MessageSender sender;

  @Override
  public String getName() {
    return DEPLOY_UPDATE_ACTION;
  }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    Map<String, String> params = ctx.getMessage().getProtocolParameters();
    if(params.containsKey("domain") && params.containsKey("contextRoot") && !memberTracker.getMe().equals(new ChannelMember(ctx.getSource()))) {
      ChannelMember me = memberTracker.getMe();
      DeploymentStatus status = tracker.getDeployment(params.get("domain"), params.get("contextRoot"));
      if(status != null) {
        List<String> myMsgs = status.getMemberLogs(me);
        boolean finished = status.getMemberFinished(me);
        CadmiumDeploymentException exception = status.exception;
        
        Message msg = new Message();
        msg.setCommand(DeployUpdateResponseCommandAction.DEPLOY_UPDATE_RESPONSE_ACTION);
        msg.getProtocolParameters().putAll(params);
        msg.getProtocolParameters().put("finished", finished + "");
        if(myMsgs != null) {
          msg.getProtocolParameters().put("msgs", new Gson().toJson(myMsgs));
        }
        if(exception != null) {
          msg.getProtocolParameters().put("exception", new Gson().toJson(exception));
        }
        
        sender.sendMessage(msg, new ChannelMember(ctx.getSource()));
      }
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {}

}
