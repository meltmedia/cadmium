package com.meltmedia.cadmium.core.messaging;

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;

import org.junit.Test;

public class MessageConverterTest {
  private static final String serializedMessage = "{\"command\":\"UPDATE\",\"protocolParameters\":{\"branch\":\"master\",\"rev\":\"HEAD\"}}";
  private static final Message deserializedMessage = new Message();
  static {
    deserializedMessage.setCommand(ProtocolMessage.UPDATE);
    deserializedMessage.setProtocolParameters(new LinkedHashMap<String, String>());
    deserializedMessage.getProtocolParameters().put("branch", "master");
    deserializedMessage.getProtocolParameters().put("rev", "HEAD");
  }

  @Test
  public void testSerialize() throws Exception {
    String serialized = MessageConverter.serialize(deserializedMessage);
    
    assertTrue("Message failed to serialize", serialized != null);
    assertTrue("Message serialized incorrectly", serializedMessage.equals(serialized));
  }
  
  @Test
  public void testDeserialize() throws Exception {
    Message deserialized = MessageConverter.deserialize(serializedMessage);
    
    assertTrue("Failed to deserialize message", deserialized != null);
    assertTrue("Wrong command", deserialized.getCommand() != null && deserializedMessage.getCommand() == deserialized.getCommand());
    assertTrue("Bad Parameters", deserialized.getProtocolParameters() != null && deserializedMessage.getProtocolParameters().equals(deserialized.getProtocolParameters()));
  }
}
