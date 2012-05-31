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

@Singleton
public class MaintenanceCommandAction implements CommandAction {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private HistoryManager manager;

	@Inject
	private SiteDownService siteDownService;
	
	@Override
	public boolean execute(CommandContext ctx) throws Exception {
		Map<String,String> params = ctx.getMessage().getProtocolParameters();
		String comment = "";
		if(params.containsKey("state") && params.get("state") != null) {
			String state = params.get("state");
			if(params.containsKey("comment") && params.get("comment") != null) {
				comment = params.get("comment");
			}
			if(state.equalsIgnoreCase("on")) {
				siteDownService.start();
			} 
			else if (state.equalsIgnoreCase("off")) {
				siteDownService.stop();
			}
		} 		
		manager.logEvent(siteDownService.isOn(),"",comment);
		return true;
	}

	@Override
	public void handleFailure(CommandContext ctx, Exception e) {
		
	}

}
