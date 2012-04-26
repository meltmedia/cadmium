package com.meltmedia.cadmium.jgroups;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.jgroups.Address;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.TimeoutException;
import org.jgroups.View;
import org.jgroups.stack.ProtocolStack;

public class DummyJChannel extends JChannel {
  
  private View testView = new View();
  private Message testMessage;

  public DummyJChannel() throws ChannelException {
  }

  @Override
  protected void _close(boolean arg0, boolean arg1) {
  }

  @Override
  public void blockOk() {
  }

  @Override
  protected void checkClosed() throws ChannelClosedException {
  }

  @Override
  protected void checkNotConnected() throws ChannelNotConnectedException {
  }

  @Override
  public synchronized void close() {
  }

  @Override
  public synchronized boolean connect(String cluster_name, Address target,
      String state_id, long timeout) throws ChannelException {
    return true;
  }

  @Override
  public synchronized void connect(String arg0) throws ChannelException,
      ChannelClosedException {
  }

  @Override
  public synchronized void disconnect() {
  }

  @Override
  public void down(Event arg0) {
  }

  @Override
  public String dumpQueue() {
    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Map dumpStats() {
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

  @SuppressWarnings("rawtypes")
  @Override
  public boolean getAllStates(Vector targets, long timeout)
      throws ChannelNotConnectedException, ChannelClosedException {
    return true;
  }

  @Override
  public String getChannelName() {
    return null;
  }

  @Override
  public String getClusterName() {
    return null;
  }

  @Override
  public Address getLocalAddress() {
    return null;
  }

  @Override
  protected Log getLog() {
    return null;
  }

  @Override
  public int getNumMessages() {
    return -1;
  }

  @Override
  public int getNumberOfTasksInTimer() {
    return -1;
  }

  @Override
  public Object getOpt(int option) {
    return null;
  }

  @Override
  public String getProperties() {
    return null;
  }

  @Override
  public ProtocolStack getProtocolStack() {
    return null;
  }

  @Override
  public long getReceivedBytes() {
    return -1l;
  }

  @Override
  public long getReceivedMessages() {
    return -1l;
  }

  @Override
  public long getSentBytes() {
    return -1l;
  }

  @Override
  public long getSentMessages() {
    return -1l;
  }

  @Override
  public boolean getState(Address target, long timeout)
      throws ChannelNotConnectedException, ChannelClosedException {
    return true;
  }

  @Override
  public boolean getState(Address target, String state_id, long timeout)
      throws ChannelNotConnectedException, ChannelClosedException {
    return true;
  }

  @Override
  public View getView() {
    return testView;
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
  public synchronized void open() throws ChannelException {
  }

  @Override
  public Object peek(long arg0) throws ChannelNotConnectedException,
      ChannelClosedException, TimeoutException {
    return null;
  }

  @Override
  public String printProtocolSpec(boolean include_properties) {
    return null;
  }

  @Override
  public Object receive(long arg0) throws ChannelNotConnectedException,
      ChannelClosedException, TimeoutException {
    return null;
  }

  @Override
  public void resetStats() {
  }

  @Override
  public void returnState(byte[] state, String state_id) {
  }

  @Override
  public void returnState(byte[] state) {
  }

  @Override
  public void send(Address dst, Address src, Serializable obj)
      throws ChannelNotConnectedException, ChannelClosedException {
  }

  @Override
  public void send(Message msg) throws ChannelNotConnectedException,
      ChannelClosedException {
    testMessage = msg;
  }
  
  public Message getLastMessage() {
    return testMessage;
  }

  @Override
  public void setOpt(int option, Object value) {
  }

  @Override
  public synchronized void shutdown() {
  }

  @Override
  public boolean startFlush(long timeout, boolean automatic_resume) {
    return true;
  }

  @Override
  public boolean startFlush(long arg0, int arg1, boolean arg2) {
    return true;
  }

  @Override
  public boolean statsEnabled() {
    return true;
  }

  @Override
  public void stopFlush() {
  }

  @Override
  public String toString(boolean details) {
    return null;
  }

  @Override
  public void up(Event arg0) {
  }

}
