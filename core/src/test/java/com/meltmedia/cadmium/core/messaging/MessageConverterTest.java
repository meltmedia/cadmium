/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
