package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Properties;

import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;

public class SyncCommandActionTest {

  @Test
  public void testCommandAsCoordinator() throws Exception {
    DummyMembershipTracker tracker = new DummyMembershipTracker();
    tracker.setMembers(new ArrayList<ChannelMember>());
    tracker.getMembers().add(new ChannelMember(new IpAddress(1234), true, true));
    tracker.getMembers().add(new ChannelMember(new IpAddress(4321), false, false));
    
    Properties configProperties = new Properties();
    configProperties.setProperty("branch", "master");
    configProperties.setProperty("git.ref.sha", "good_key");
    
    DummyMessageSender sender = new DummyMessageSender();
    
    SyncCommandAction cmd = new SyncCommandAction();
    cmd.configProperties = configProperties;
    cmd.tracker = tracker;
    cmd.sender = sender;
    
    CommandContext ctx = new CommandContext(new IpAddress(4321), new Message());
    ctx.getMessage().setCommand(ProtocolMessage.SYNC);
    
    assertTrue("Command failed to execute", cmd.execute(ctx));
    
    assertTrue("Message not sent", sender.dest != null && sender.msg != null && sender.dest.getAddress() == ctx.getSource());
    assertTrue("Message not sync", sender.msg.getCommand() == ProtocolMessage.SYNC);
    assertTrue("Incorrent branch", sender.msg.getProtocolParameters().containsKey("branch")
        && sender.msg.getProtocolParameters().get("branch").equals("master"));

    assertTrue("Incorrent sha", sender.msg.getProtocolParameters().containsKey("sha")
        && sender.msg.getProtocolParameters().get("sha").equals("good_key"));
    
  }

  @Test
  public void testCommandAsNotCoordinator() throws Exception {
    DummyMembershipTracker tracker = new DummyMembershipTracker();
    tracker.setMembers(new ArrayList<ChannelMember>());
    tracker.getMembers().add(new ChannelMember(new IpAddress(1234), false, true));
    tracker.getMembers().add(new ChannelMember(new IpAddress(4321), true, false));
    
    final DummySiteDownService maintFilter = new DummySiteDownService();
    DummyCoordinatedWorker worker = new DummyCoordinatedWorker();
    DummyContentService fileServlet = new DummyContentService();
    CoordinatedWorkerListener listener = new CoordinatedWorkerListener() {

      @Override
      public void workDone() {
      }

      @Override
      public void workFailed() {
      }
      
    };
    worker.setListener(listener);
    
    Properties configProperties = new Properties();
    configProperties.setProperty("branch", "master");
    configProperties.setProperty("git.ref.sha", "old_key");
    
    DummyMessageSender sender = new DummyMessageSender();
    
    SyncCommandAction cmd = new SyncCommandAction();
    cmd.configProperties = configProperties;
    cmd.tracker = tracker;
    cmd.sender = sender;
    cmd.maintFilter = maintFilter;
    cmd.worker = worker;
    cmd.fileServlet = fileServlet;
    
    CommandContext ctx = new CommandContext(new IpAddress(4321), new Message());
    ctx.getMessage().setCommand(ProtocolMessage.SYNC);
    ctx.getMessage().getProtocolParameters().put("branch", "master");
    ctx.getMessage().getProtocolParameters().put("git.ref.sha", "good_key");
    
    assertTrue("Command failed to execute", cmd.execute(ctx));
    
    assertTrue("Message sent", sender.dest == null && sender.msg == null);
    assertTrue("Maint Filter didn't behave as expected", maintFilter.didStart && maintFilter.didStop);
    assertTrue("Content didn't switch", fileServlet.switched);
    assertTrue("Listener not returned to original", worker.listener == listener);    
    
  }
}
