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
