package com.meltmedia.cadmium.core.messaging;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.commands.DummyCommandAction;

public class MessageReceiverTest {

  @Test
  public void testCommandPass() throws Exception {
    MessageReceiver msg = new MessageReceiver();
    msg.commandMap = new HashMap<String, CommandAction> ();
    msg.commandMap.put(ProtocolMessage.CURRENT_STATE, new DummyCommandAction(true, false));
    msg.commandMap.put(ProtocolMessage.STATE_UPDATE, new DummyCommandAction(true, false));
    msg.commandMap.put(ProtocolMessage.SYNC, new DummyCommandAction(true, false));
    msg.commandMap.put(ProtocolMessage.UPDATE, new DummyCommandAction(true, false));
    msg.commandMap.put(ProtocolMessage.UPDATE_DONE, new DummyCommandAction(false, false));
    msg.commandMap.put(ProtocolMessage.UPDATE_FAILED, new DummyCommandAction(true, false));
    
    msg.receive(new org.jgroups.Message(null, null, "{\"command\":\""+ProtocolMessage.UPDATE_DONE+"\"}"));
    
  }

  @Test
  public void testCommandFail() throws Exception {
    MessageReceiver msg = new MessageReceiver();
    msg.commandMap = new HashMap<String, CommandAction> ();
    msg.commandMap.put(ProtocolMessage.CURRENT_STATE, new DummyCommandAction(true, false));
    msg.commandMap.put(ProtocolMessage.STATE_UPDATE, new DummyCommandAction(true, false));
    msg.commandMap.put(ProtocolMessage.SYNC, new DummyCommandAction(true, false));
    msg.commandMap.put(ProtocolMessage.UPDATE, new DummyCommandAction(true, false));
    msg.commandMap.put(ProtocolMessage.UPDATE_DONE, new DummyCommandAction(false, false));
    msg.commandMap.put(ProtocolMessage.UPDATE_FAILED, new DummyCommandAction(true, false));
    
    try {
      msg.receive(new org.jgroups.Message(null, null, "{\"command\":\""+ProtocolMessage.CURRENT_STATE+"\"}"));
      assertTrue("This shouldn't have run", false);
    } catch(Error e) {}
  }
}
