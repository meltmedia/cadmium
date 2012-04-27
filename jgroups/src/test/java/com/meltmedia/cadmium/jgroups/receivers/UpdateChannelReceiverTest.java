package com.meltmedia.cadmium.jgroups.receivers;

import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.jgroups.DummyCoordinatedWorker;
import com.meltmedia.cadmium.jgroups.DummyJChannel;

public class UpdateChannelReceiverTest {

  @Test
  public void testStateProgression() throws Exception {
    Address me = new IpAddress(12345);
    Address other = new IpAddress(54321);
    Vector<Address> viewMems = new Vector<Address>();
    viewMems.add(me);
    viewMems.add(other);
    DummyJChannel channel = new DummyJChannel(me, viewMems);
    
    DummyCoordinatedWorker worker = new DummyCoordinatedWorker();
    
    UpdateChannelReceiver receiver = new UpdateChannelReceiver(channel, worker);
    receiver.setMyState(UpdateChannelReceiver.UpdateState.IDLE);
    
    receiver.receive(new Message(null, null, "UPDATE branch=master;rev=head"));
    
    assertTrue("Worker not called!", worker.isPulling() && !worker.isKilled() && !worker.isSwitched());
    assertTrue("Message not sent", channel.getLastMessage() != null && channel.getLastMessage().getObject().equals(UpdateChannelReceiver.UpdateState.UPDATING.name()));
    assertTrue("Status not updated", receiver.getMyState() == UpdateChannelReceiver.UpdateState.UPDATING);
    
    receiver.receive(channel.getLastMessage());
    
    assertTrue("Worker not called!", worker.isPulling() && !worker.isKilled() && !worker.isSwitched());
    assertTrue("Message not sent", channel.getLastMessage() != null && channel.getLastMessage().getObject().equals(UpdateChannelReceiver.UpdateState.UPDATING.name()));
    assertTrue("Status not updated", receiver.getMyState() == UpdateChannelReceiver.UpdateState.UPDATING);
    
    receiver.workDone();
    
    assertTrue("Receiver not waiting", receiver.getMyState() == UpdateChannelReceiver.UpdateState.WAITING);
    assertTrue("Update done message not sent", channel.getLastMessage() != null && channel.getLastMessage().getObject().equals(UpdateChannelReceiver.ProtocolMessage.UPDATE_DONE.name()));
    
    receiver.receive(channel.getLastMessage());

    assertTrue("Receiver state changed", receiver.getMyState() == UpdateChannelReceiver.UpdateState.WAITING);
    assertTrue("Update done didn't trigger state message", channel.getLastMessage() != null && channel.getLastMessage().getObject().equals(UpdateChannelReceiver.UpdateState.WAITING.name()));
    
    receiver.receive(channel.getLastMessage());
    
    assertTrue("Message not sent", channel.getLastMessage() != null && channel.getLastMessage().getObject().equals(UpdateChannelReceiver.UpdateState.WAITING.name()));
    assertTrue("Status not updated", receiver.getMyState() == UpdateChannelReceiver.UpdateState.WAITING);
    
    channel.send(null);
    
    receiver.receive(new Message(null, other, UpdateChannelReceiver.UpdateState.WAITING));
    
    assertTrue("Worker not called!", worker.isPulling() && !worker.isKilled() && worker.isSwitched());
    assertTrue("A Message was sent", channel.getLastMessage() == null);
    assertTrue("Status not updated", receiver.getMyState() == UpdateChannelReceiver.UpdateState.IDLE);
  }
}
