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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.logging.Log;
import org.jgroups.stack.AddressGenerator;
import org.jgroups.stack.ProtocolStack;

public class DummyJChannel extends JChannel {
  
  private View testView = null;
  private Address localAddress = null;
  private List<Message> testMessage = new ArrayList<Message>();
  

  public DummyJChannel(Address localAddress, Vector<Address> viewMembers) throws Exception {
    super();
    this.localAddress = localAddress;
    long viewId = 1000l;
    this.testView = new View(viewMembers.get(0), viewId, viewMembers);
  }
  
  @Override
  protected void _close(boolean disconnect) {
  }
  @Override
  protected void checkClosed() {
  }
  @Override
  protected void checkClosedOrNotConnected() {
  }
  @Override
  public synchronized void close() {
  }
  @Override
  public synchronized void connect(String arg0, Address arg1, long arg2,
      boolean arg3) throws Exception {
  }
  @Override
  public synchronized void connect(String cluster_name, Address target,
      long timeout) throws Exception {
  }
  @Override
  protected synchronized void connect(String arg0, boolean arg1)
      throws Exception {
  }
  @Override
  public synchronized void connect(String cluster_name) throws Exception {
  }
  @Override
  public synchronized void disconnect() {
  }
  @Override
  public Object down(Event evt) {
    return null;
  }
  @Override
  protected Map<String, Long> dumpChannelStats() {
    return null;
  }
  @Override
  public Map<String, Object> dumpStats() {
    return null;
  }
  @Override
  public Map<String, Object> dumpStats(String protocol_name, List<String> attrs) {
    return null;
  }
  @Override
  public Map<String, Object> dumpStats(String protocol_name) {
    return null;
  }
  @Override
  public String dumpTimerQueue() {
    return null;
  }
  @Override
  public void enableStats(boolean stats) {
  }
  @Override
  public boolean flushSupported() {
    return true;
  }
  @Override
  public Address getAddress() {
    return this.localAddress;
  }
  @Override
  public String getAddressAsString() {
    return getAddress().toString();
  }
  @Override
  public String getAddressAsUUID() {
    return getAddress().toString();
  }
  @Override
  public AddressGenerator getAddressGenerator() {
    return null;
  }
  @Override
  public String getClusterName() {
    return "test";
  }
  @Override
  protected Log getLog() {
    return null;
  }
  @Override
  public String getName() {
    return this.localAddress.toString();
  }
  @Override
  public String getName(Address member) {
    return member.toString();
  }
  @Override
  public int getNumberOfTasksInTimer() {
    return 0;
  }
  @Override
  public String getProperties() {
    return "";
  }
  @Override
  public ProtocolStack getProtocolStack() {
    return null;
  }
  @Override
  public long getReceivedBytes() {
    return 1l;
  }
  @Override
  public long getReceivedMessages() {
    return 1l;
  }
  @Override
  public long getSentBytes() {
    return 1l;
  }
  @Override
  public long getSentMessages() {
    return this.testMessage.size();
  }
  @Override
  public void getState(Address target, long timeout, boolean useFlushIfPresent)
      throws Exception {
  }
  @Override
  protected void getState(Address arg0, long arg1, Callable<Boolean> arg2)
      throws Exception {
  }
  @Override
  public void getState(Address target, long timeout) throws Exception {
  }
  @Override
  public int getTimerThreads() {
    return 1;
  }
  @Override
  public View getView() {
    return this.testView;
  }
  @Override
  public String getViewAsString() {
    return this.testView.toString();
  }
  @Override
  protected Object invokeCallback(int arg0, Object arg1) {
    return null;
  }
  @Override
  public boolean isConnected() {
    return true;
  }
  @Override
  public boolean isOpen() {
    return true;
  }
  @Override
  public String printProtocolSpec(boolean include_properties) {
    return "";
  }
  @Override
  public void resetStats() {
  }
  @Override
  public void send(Address dst, byte[] buf, int offset, int length)
      throws Exception {
    send(new Message(dst, buf, offset, length));
  }
  @Override
  public void send(Address dst, byte[] buf) throws Exception {
    send(new Message(dst, buf));
  }
  @Override
  public void send(Address dst, Object obj) throws Exception {
    send(new Message(dst, obj));
  }
  @Override
  public void send(Message msg) throws Exception {
    testMessage.add(msg);
  }
  @Override
  protected void setAddress() {
  }
  @Override
  public void setAddressGenerator(AddressGenerator address_generator) {
  }
  @Override
  public void setName(String name) {
  }
  @Override
  public void setProtocolStack(ProtocolStack stack) {
  }
  @Override
  public void startFlush(boolean arg0) throws Exception {
  }
  @Override
  public void startFlush(List<Address> arg0, boolean arg1) throws Exception {
  }
  @Override
  public boolean statsEnabled() {
    return true;
  }
  @Override
  public void stopFlush() {
  }
  @Override
  public void stopFlush(List<Address> flushParticipants) {
  }
  @Override
  protected void stopStack(boolean arg0, boolean arg1) {
    
  }
  @Override
  public String toString(boolean details) {
    return "";
  }
  @Override
  public Object up(Event arg0) {
    return null;
  }

  public View getTestView() {
    return testView;
  }

  public Address getLocalAddress() {
    return localAddress;
  }

  public List<Message> getMessageList() {
    return testMessage;
  }
}
