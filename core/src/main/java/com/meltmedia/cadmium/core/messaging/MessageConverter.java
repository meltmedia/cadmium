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

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class MessageConverter {


  private static ObjectMapper mapper = new ObjectMapper();
  static {
    // this drops null fields from the messages.  Inclusion.NON_DEFAULT may be better.
    mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion(Inclusion.NON_NULL));
  }
  private static JsonFactory factory = mapper.getJsonFactory();
  
  @Inject
  @Named("commandBodyMap")
  protected Map<String, Class<?>> commandToBodyMapping;
  
  public Map<String, Class<?>> getCommandToBodyMapping() {
    return commandToBodyMapping;
  }

  public void setCommandToBodyMapping(Map<String, Class<?>> commandToBodyMapping) {
    this.commandToBodyMapping = commandToBodyMapping;
  }
  
  public org.jgroups.Message toJGroupsMessage(Message<?> cMessage) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator = factory.createJsonGenerator(out);
    generator.writeStartObject();
    generator.writeObjectField("header", cMessage.getHeader());
    if( cMessage.getBody() != null ) {
      generator.writeObjectField("body", cMessage.getBody());
    }
    generator.writeEndObject();
    generator.close();
    
    org.jgroups.Message jgMessage = new org.jgroups.Message();
    jgMessage.setBuffer(out.toByteArray());
    return jgMessage;
  }
  
  public <B> Message<B> toCadmiumMessage(org.jgroups.Message jgMessage) throws JsonProcessingException, IOException {
    JsonParser parser = factory.createJsonParser(jgMessage.getBuffer());
    parser.nextToken(); // parse the start token for the document.
    parser.nextToken(); // parse the field name
    parser.nextToken(); // parse the start token for header.
    Header header = parser.readValueAs(Header.class);
    Class<?> bodyClass = lookupBodyClass(header);
    parser.nextToken(); // parse the end token for header.
    
    Object body = null;
    if( bodyClass == Void.class ) {
      body = null;
    }
    else {
      parser.nextToken(); // parse the start token for body.
      body = parser.readValueAs(bodyClass);
      parser.nextToken(); // the end token for body.
    }
    parser.nextToken(); // the end token for the document.
    parser.close();
    
    @SuppressWarnings("unchecked")
    Message<B> cMessage = new Message<B>(header, (B)body);
    return cMessage;
  }
  
  private Class<?> lookupBodyClass(Header header) throws IOException {
    if( header == null ) throw new IOException("Could not deserialize message body: no header.");
    if( header.getCommand() == null ) throw new IOException("Could not deserialize message body: no command declared.");
    Class<?> commandBodyClass = commandToBodyMapping.get(header.getCommand());
    if( commandBodyClass == null ) throw new IOException("Could not deserialize message body: no body class defined for "+header.getCommand()+".");
    return commandBodyClass;
  }
   
}
