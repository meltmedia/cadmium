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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.meltmedia.cadmium.core.commands.ContentUpdateRequest;
import com.meltmedia.cadmium.core.commands.GitLocation;
import com.meltmedia.cadmium.core.messaging.Header;

public class MessageConverterTest {
  private static final String serializedMessage = "{\"header\":{\"command\":\"UPDATE\",\"requestTime\":233434800},\"body\":{\"contentLocation\":{\"branch\":\"master\",\"revision\":\"HEAD\"},\"revertable\":false}}";
  private static final String serializedNullBodyMessage = "{\"header\":{\"command\":\"CURRENT_STATE\",\"requestTime\":233434800}}";
  private static final Message<ContentUpdateRequest> deserializedMessage = new Message<ContentUpdateRequest>();
  private static final Message<Void> deserializedNullBodyMessage = new Message<Void>();
  private static final MessageConverter converter = new MessageConverter();
  static {
    Header header = new Header(ProtocolMessage.UPDATE);
    header.setRequestTime(new Long(233434800));
    ContentUpdateRequest body = new ContentUpdateRequest();
    body.setContentLocation(new GitLocation(null, "master", "HEAD"));
    deserializedMessage.setHeader(header);
    deserializedMessage.setBody(body);
    
    Header headerNullBody = new Header(ProtocolMessage.CURRENT_STATE);
    headerNullBody.setRequestTime(new Long(233434800));
    deserializedNullBodyMessage.setHeader(headerNullBody);
    deserializedNullBodyMessage.setBody(null);
    
    Map<String, Class<?>> commandToBodyMap = new HashMap<String, Class<?>>();
    commandToBodyMap.put(ProtocolMessage.UPDATE, ContentUpdateRequest.class);
    commandToBodyMap.put(ProtocolMessage.CURRENT_STATE, Void.class);
    converter.setCommandToBodyMapping(commandToBodyMap);
  }

  @Test
  public void testSerialize() throws Exception {
    MessageConverter converter = new MessageConverter();
    org.jgroups.Message serialized = converter.toJGroupsMessage(deserializedMessage);
    
    assertTrue("Message failed to serialize", serialized != null);
    assertEquals("Message serialized incorrectly", serializedMessage, new String(serialized.getBuffer(), "UTF-8"));
  }
  
  @Test
  public void testSerializeNull() throws Exception {
    MessageConverter converter = new MessageConverter();
    org.jgroups.Message serialized = converter.toJGroupsMessage(deserializedNullBodyMessage);
    
    assertTrue("Message failed to serialize", serialized != null);
    assertEquals("Message serialized incorrectly", serializedNullBodyMessage, new String(serialized.getBuffer(), "UTF-8"));
  }
  
  @Test
  public void testDeserialize() throws Exception {
    org.jgroups.Message serialized = new org.jgroups.Message();
    serialized.setBuffer(serializedMessage.getBytes("UTF-8"));
    
    Message<ContentUpdateRequest> deserialized = converter.toCadmiumMessage(serialized);
    
    assertTrue("Failed to deserialize message", deserialized != null);
    assertEquals("Wrong command", deserializedMessage.getHeader().getCommand(), deserialized.getHeader().getCommand());
    assertEquals("Wrong requestTime", deserializedMessage.getHeader().getRequestTime(), deserialized.getHeader().getRequestTime());
    assertNotNull("Deserialized body is null", deserialized.getBody());
    assertEquals("Incorrect branch name.", deserializedMessage.getBody().getContentLocation().getBranch(), deserialized.getBody().getContentLocation().getBranch());
  }
  
  @Test
  public void testDeserializeNullBody() throws Exception {
    org.jgroups.Message serialized = new org.jgroups.Message();
    serialized.setBuffer(serializedNullBodyMessage.getBytes("UTF-8"));
    
    Message<ContentUpdateRequest> deserialized = converter.toCadmiumMessage(serialized);
    
    assertTrue("Failed to deserialize message", deserializedNullBodyMessage != null);
    assertEquals("Wrong command", deserializedNullBodyMessage.getHeader().getCommand(), deserialized.getHeader().getCommand());
    assertEquals("Wrong requestTime", deserializedNullBodyMessage.getHeader().getRequestTime(), deserialized.getHeader().getRequestTime());
    assertNull("Deserialized body is null", deserialized.getBody());
  }
}
