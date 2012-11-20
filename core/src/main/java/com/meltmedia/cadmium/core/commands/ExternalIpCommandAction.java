package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

/**
 * This command action will process messages to update the external ip address of a member of this cluster.
 * 
 * @author John McEntire
 *
 */
public class ExternalIpCommandAction implements CommandAction<ExternalIpMessage> {
  
  @Inject
  protected MembershipTracker membershipTracker;

  @Override
  public String getName() {
    return ProtocolMessage.EXTERNAL_IP_MESSAGE;
  }

  @Override
  public boolean execute(CommandContext<ExternalIpMessage> ctx)
      throws Exception {
    if(StringUtils.isNotBlank(ctx.getMessage().getBody().getIp())){
      membershipTracker.updateMembersIp(ctx.getSource(), ctx.getMessage().getBody().getIp());
    }
    return true;
  }

  @Override
  public void handleFailure(CommandContext<ExternalIpMessage> ctx, Exception e) {}

}
