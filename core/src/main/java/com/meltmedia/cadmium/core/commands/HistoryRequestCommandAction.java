package com.meltmedia.cadmium.core.commands;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.history.HistoryEntry;
import com.meltmedia.cadmium.core.history.HistoryManager;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

@Singleton
public class HistoryRequestCommandAction implements CommandAction {
  private final Logger log = LoggerFactory.getLogger(getClass());  
  
  @Inject
  protected HistoryManager historyManager;
  
  @Inject
  protected MessageSender sender;
  
  public String getName() { return ProtocolMessage.HISTORY_REQUEST; };

  @Override
  public boolean execute(CommandContext ctx) throws Exception {
    Message response = new Message();
    response.setCommand(ProtocolMessage.HISTORY_RESPONSE);
    int limit = -1;
    if(ctx.getMessage().getProtocolParameters().containsKey("limit")) {
      limit = Integer.parseInt(ctx.getMessage().getProtocolParameters().get("limit"));
    }
    boolean filter = Boolean.parseBoolean(ctx.getMessage().getProtocolParameters().get("filter"));
    List<HistoryEntry> history = historyManager.getHistory(limit, filter);
    log.info("Received history-request message responding with {} history items", history.size());
    Gson gson = new Gson();
    response.getProtocolParameters().put("history", gson.toJson(history, new TypeToken<List<HistoryEntry>>(){}.getType()));
    sender.sendMessage(response, new ChannelMember(ctx.getSource()));
    return true;
  }

  @Override
  public void handleFailure(CommandContext ctx, Exception e) {

  }

}
