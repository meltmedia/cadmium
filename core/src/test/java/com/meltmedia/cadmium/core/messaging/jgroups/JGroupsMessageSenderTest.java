package com.meltmedia.cadmium.core.messaging.jgroups;

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class JGroupsMessageSenderTest {
  private static final String serializedMessage = "{\"command\":\"UPDATE\",\"protocolParameters\":{\"branch\":\"master\",\"rev\":\"HEAD\"}}";
  private static final Message deserializedMessage = new Message();
  static {
    deserializedMessage.setCommand(ProtocolMessage.UPDATE);
    deserializedMessage.setProtocolParameters(new LinkedHashMap<String, String>());
    deserializedMessage.getProtocolParameters().put("branch", "master");
    deserializedMessage.getProtocolParameters().put("rev", "HEAD");
  }

  @Test
  public void testSendMessage() throws Exception {
    Address me = new IpAddress(12345);
    Vector<Address> viewMems = new Vector<Address>();
    viewMems.add(me);
    
    DummyJChannel channel = new DummyJChannel(me, viewMems);
    
    JGroupsMessageSender sender = new JGroupsMessageSender();
    sender.channel = channel;
    
    sender.sendMessage(deserializedMessage, new ChannelMember(me));
    
    assertTrue("Failed to send message", channel.getMessageList().size() == 1 && channel.getMessageList().get(0) != null);
    assertTrue("Incorrect Destination", channel.getMessageList().get(0).getDest().toString().equals(me.toString()));
    assertTrue("Incorrect Message sent", channel.getMessageList().get(0).getObject().toString().equals(serializedMessage));
  }
}
