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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.SiteDownService;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class MaintenanceCommandAction implements CommandAction {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	protected HistoryManager manager;

	@Inject
	protected SiteDownService siteDownService;
	
  public String getName() { return ProtocolMessage.MAINTENANCE; }
	
	@Override
	public boolean execute(CommandContext ctx) throws Exception {
		log.info("Beginning Maintenance Toggle, started by {}", ctx.getSource());
		Map<String,String> params = ctx.getMessage().getProtocolParameters();
		String comment = "";
		if(params.containsKey("state") && params.get("state") != null) {
			String state = params.get("state");
			if(params.containsKey("comment") && params.get("comment") != null) {
				comment = params.get("comment");
			}
			if(state.equalsIgnoreCase("on")) {
			  log.info("Starting maintenance page.");
				siteDownService.start();
			} 
			else if (state.equalsIgnoreCase("off")) {
        log.info("Stopping maintenance page.");
				siteDownService.stop();
			}
		} 		
		manager.logEvent(siteDownService.isOn(),ctx.getMessage().getProtocolParameters().get("openId"),comment);
		return true;
	}

	@Override
	public void handleFailure(CommandContext ctx, Exception e) {
		
	}

}
