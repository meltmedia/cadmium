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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jgroups.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CommandAction;
import com.meltmedia.cadmium.core.CommandContext;

@Singleton
public class MessageReceiver implements MessageListener {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  @Named("commandMap")
  Map<String, CommandAction> commandMap;
  
  @Inject
  JacksonMessageConverter converter;
  
  @Override
  public void receive(org.jgroups.Message msg) {
    JacksonMessage<?> message = null;
    try {
      message = converter.toCadmiumMessage(msg);
    }
    catch( Exception e ) {
      log.error("Failed to parse message.", e);
    }
    
    CommandContext ctx = new CommandContext(msg.getSrc(), message);
    String command = message.getHeader().getCommand();
    CommandAction action = commandMap.get(command);
    if( action == null ) return;

    try {
      if (!action.execute(ctx)) {
        action.handleFailure(ctx, null);
      }
    } catch (Exception e) {
      action.handleFailure(ctx, e);
      log.error("Command [{}] failed: {}", command, e.getMessage());
    }

  }

  @Override
  public void getState(OutputStream arg0) throws Exception {
    
  }

  @Override
  public void setState(InputStream arg0) throws Exception {
    
  }

}
