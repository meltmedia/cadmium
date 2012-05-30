package com.meltmedia.cadmium.core.commands;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.SiteDownService;

@Singleton
public class MaintenanceCommandAction implements CommandAction {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private SiteDownService siteDownService;
	
	@Override
	public boolean execute(CommandContext ctx) throws Exception {
		Map<String,String> params = ctx.getMessage().getProtocolParameters();
		if(params.containsKey("state")) {
			String state = params.get("state");
			if(state.equalsIgnoreCase("on")) {
				siteDownService.start();
			} else if (state.equalsIgnoreCase("off")) {
				siteDownService.stop();
			}
		} 		
		return true;
	}

	@Override
	public void handleFailure(CommandContext ctx, Exception e) {
		
	}

}
