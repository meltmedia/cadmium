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
package com.meltmedia.cadmium.core.commands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class CurrentStateCommandAction implements CommandAction<Void> {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected LifecycleService lifecycleService;
  
  public String getName() { return ProtocolMessage.CURRENT_STATE; };

  @Override
  public boolean execute(CommandContext<Void> ctx) throws Exception {
    log.info("Responding with current state {}", lifecycleService.getCurrentState());
    lifecycleService.sendStateUpdate(new ChannelMember(ctx.getSource()));
    log.info("Responding with current config state {}", lifecycleService.getCurrentConfigState());
    lifecycleService.sendConfigStateUpdate(new ChannelMember(ctx.getSource()));
    return true;
  }

  @Override
  public void handleFailure(CommandContext<Void> ctx, Exception e) {

  }

}
