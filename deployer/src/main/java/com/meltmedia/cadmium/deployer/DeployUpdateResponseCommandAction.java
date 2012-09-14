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
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.deployer.DeploymentTracker.DeploymentStatus;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;

/**
 * A CommandAction that updates the {@link DeploymentTracker} with the responses from the 
 * other nodes in a cluster.
 * 
 * @author John McEntire
 *
 */
public class DeployUpdateResponseCommandAction implements CommandAction {
  
  public static final String DEPLOY_UPDATE_RESPONSE_ACTION = "DEPLOY_UPDATE_RESPONSE";
  
  @Inject
  protected DeploymentTracker tracker;
  
  @Inject
  protected MembershipTracker memberTracker;

  @Override
  public String getName() {
    return DEPLOY_UPDATE_RESPONSE_ACTION;
  }

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    Map<String, String> params = ctx.getMessage().getProtocolParameters();
    if(params.containsKey("domain") && params.containsKey("contextRoot") && !memberTracker.getMe().equals(new ChannelMember(ctx.getSource()))) {
      DeploymentStatus status = tracker.getDeployment(params.get("domain"), params.get("contextRoot"));
      if(status != null) {
        boolean finished = new Boolean(params.get("finished")).booleanValue();
        List<String> msgs = null;
        if(params.containsKey("msgs")) {
          msgs = new Gson().fromJson(params.get("msgs"), new TypeToken<List<String>>() {}.getType());
        }
        CadmiumDeploymentException exception = null;
        if(params.containsKey("exception")) {
          exception = new Gson().fromJson(params.get("exception"), CadmiumDeploymentException.class);
        }
        synchronized(status) {
          ChannelMember mem = memberTracker.getMember(new ChannelMember(ctx.getSource()));
          if(finished) {
            status.setMemberFinished(mem);
          } else {
            status.setMemberStarted(mem);
          }
          if(msgs != null && msgs.size() > 0) {
            List<String> oldMsgs = status.getMemberLogs(mem);
            oldMsgs.clear();
            oldMsgs.addAll(msgs);
          } else {
            status.getMemberLogs(mem).clear();
          }
          if(exception != null) {
            status.exception = exception;
          }
        }
      }
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {}

}
