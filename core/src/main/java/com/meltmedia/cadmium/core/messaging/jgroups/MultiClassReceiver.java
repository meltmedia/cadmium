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
package com.meltmedia.cadmium.core.messaging.jgroups;

import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MultiClassReceiver implements Receiver, MessageListener, MembershipListener {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  protected MessageListener messageListener;
  
  protected MembershipListener membershipListener;
  
  @Inject
  public MultiClassReceiver(MessageListener messageListener, MembershipListener membershipListener, JChannel channel) {
    this.messageListener = messageListener;
    this.membershipListener = membershipListener;
    channel.setReceiver(this);
  }

  @Override
  public void block() {
    membershipListener.block();
  }

  @Override
  public void suspect(Address arg0) {
    membershipListener.suspect(arg0);
  }

  @Override
  public void viewAccepted(View arg0) {
    log.trace("Received updated view deligating to {"+this.membershipListener+"}");
    membershipListener.viewAccepted(arg0);
  }

  @Override
  public void receive(Message arg0) {
    log.trace("Received message deligating to {"+this.messageListener+"}");
    messageListener.receive(arg0);
  }

  @Override
  public void unblock() {
    membershipListener.unblock();
  }

  @Override
  public void getState(OutputStream arg0) throws Exception {
    messageListener.getState(arg0);
  }

  @Override
  public void setState(InputStream arg0) throws Exception {
    messageListener.setState(arg0);
  }

}
