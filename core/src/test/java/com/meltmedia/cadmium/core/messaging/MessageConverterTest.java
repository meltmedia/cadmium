package com.meltmedia.cadmium.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;

import org.junit.Test;

public class MessageConverterTest {
  private static final String serializedMessage = "{\"command\":\"UPDATE\",\"protocolParameters\":{\"branch\":\"master\",\"rev\":\"HEAD\"},\"requestTime\":233434800}";
  private static final Message deserializedMessage = new Message();
  static {
    deserializedMessage.setCommand(ProtocolMessage.UPDATE);
    deserializedMessage.setRequestTime(new Long(233434800));
    deserializedMessage.setProtocolParameters(new LinkedHashMap<String, String>());
    deserializedMessage.getProtocolParameters().put("branch", "master");
    deserializedMessage.getProtocolParameters().put("rev", "HEAD");
  }

  @Test
  public void testSerialize() throws Exception {
    String serialized = MessageConverter.serialize(deserializedMessage);
    
    assertTrue("Message failed to serialize", serialized != null);
    assertEquals("Message serialized incorrectly", serializedMessage, serialized);
  }
  
  @Test
  public void testDeserialize() throws Exception {
    Message deserialized = MessageConverter.deserialize(serializedMessage);
    
    assertTrue("Failed to deserialize message", deserialized != null);
    assertEquals("Wrong command", deserializedMessage.getCommand(), deserialized.getCommand());
    assertEquals("Wrong requestTime", deserializedMessage.getRequestTime(), deserialized.getRequestTime());
    assertTrue("Bad Parameters", deserialized.getProtocolParameters() != null && deserializedMessage.getProtocolParameters().equals(deserialized.getProtocolParameters()));
  }
}
