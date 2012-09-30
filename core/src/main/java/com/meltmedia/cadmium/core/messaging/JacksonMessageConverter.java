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

public class JacksonMessageConverter {
  private static JsonFactory factory = new JsonFactory();
  
  @Inject
  @Named("commandBodyMap")
  protected Map<String, Class<?>> commandToBodyMapping;
  
  public org.jgroups.Message toJGroupsMessage(JacksonMessage<?> cMessage) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator = factory.createJsonGenerator(out);
    generator.writeObjectField("headers", cMessage.getHeader());
    generator.writeObjectField("body", cMessage.getBody());
    generator.close();
    
    org.jgroups.Message jgMessage = new org.jgroups.Message();
    jgMessage.setBuffer(out.toByteArray());
    return jgMessage;
  }
  
  public JacksonMessage<?> toCadmiumMessage(org.jgroups.Message jgMessage) throws JsonProcessingException, IOException {
    JsonParser parser = factory.createJsonParser(jgMessage.getBuffer());
    JacksonMessage.Header header = parser.readValueAs(JacksonMessage.Header.class);
    Object body = parser.readValueAs(commandToBodyMapping.get(header.getCommand()));
    
    JacksonMessage<Object> cMessage = new JacksonMessage<Object>();
    cMessage.setHeader(header);
    cMessage.setBody(body);
    parser.close();
    return cMessage;
  }
  
}
