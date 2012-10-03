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
package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.jgroups.stack.IpAddress;
import org.junit.Before;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandContext;
import com.meltmedia.cadmium.core.CoordinatedWorkerListener;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.DummyMessageSender;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;

import static org.mockito.Mockito.*;

public class SyncCommandActionTest {  
  
  ConfigManager configManager;
  Properties configProperties = new Properties();

  @Before
  public void setupConfigManager() throws Exception {
    configManager = mock(ConfigManager.class);      

    when(configManager.getDefaultProperties()).thenReturn(configProperties);
  }
  
  @Test
  public void testCommandAsCoordinator() throws Exception {    
   
    
    DummyMembershipTracker tracker = new DummyMembershipTracker();
    tracker.setConfigManager(configManager);
    tracker.setMembers(new ArrayList<ChannelMember>());
    tracker.getMembers().add(new ChannelMember(new IpAddress(1234), true, true));
    tracker.getMembers().add(new ChannelMember(new IpAddress(4321), false, false));    
   
    configProperties.setProperty("repo", "oldRepo");
    configProperties.setProperty("branch", "master");
    configProperties.setProperty("git.ref.sha", "good_key");
    
    DummyMessageSender<SyncRequest, Void> sender = new DummyMessageSender<SyncRequest, Void>();
    
    SyncCommandAction cmd = new SyncCommandAction();
    cmd.configManager = configManager;   
    cmd.tracker = tracker;
    cmd.sender = sender;
    
    SyncRequest request = new SyncRequest();
    CommandContext<SyncRequest> ctx = new CommandContext<SyncRequest>(new IpAddress(4321), new Message<SyncRequest>(ProtocolMessage.SYNC, request));
    
    assertTrue("Command failed to execute", cmd.execute(ctx));
    
    assertTrue("Message not sent", sender.dest != null && sender.msg != null && sender.dest.getAddress() == ctx.getSource());
    assertTrue("Message not sync", sender.msg.getHeader().getCommand() == ProtocolMessage.SYNC);
    assertEquals("Incorrent repo", "oldRepo", sender.msg.getBody().getRepo());
    assertEquals("Incorrent branch", "master", sender.msg.getBody().getBranch());
    assertEquals("Incorrect sha", "good_key", sender.msg.getBody().getSha());    
  }

  @Test
  public void testCommandAsNotCoordinator() throws Exception {
    DummyMembershipTracker tracker = new DummyMembershipTracker();
    tracker.setConfigManager(configManager);
    tracker.setMembers(new ArrayList<ChannelMember>());
    tracker.getMembers().add(new ChannelMember(new IpAddress(1234), false, true));
    tracker.getMembers().add(new ChannelMember(new IpAddress(4321), true, false));
    
    final DummySiteDownService maintFilter = new DummySiteDownService();
    DummyCoordinatedWorker<ContentUpdateRequest> worker = new DummyCoordinatedWorker<ContentUpdateRequest>();
    DummyContentService fileServlet = new DummyContentService();
    CoordinatedWorkerListener<ContentUpdateRequest> listener = new CoordinatedWorkerListener<ContentUpdateRequest>() {

      @Override
      public void workDone(ContentUpdateRequest body) {
      }

      @Override
      public void workFailed(ContentUpdateRequest body) {
      }
      
    };

    worker.setListener(listener);    

    configProperties.setProperty("repo", "oldRepo");
    configProperties.setProperty("branch", "master");
    configProperties.setProperty("git.ref.sha", "old_key");
    
    DummyMessageSender sender = new DummyMessageSender();
    
    SyncCommandAction cmd = new SyncCommandAction();
    cmd.configManager = configManager;    
    cmd.tracker = tracker;
    cmd.sender = sender;
    cmd.maintFilter = maintFilter;
    cmd.worker = worker;
    cmd.fileServlet = fileServlet;
    cmd.processor = mock(SiteConfigProcessor.class);
    
    SyncRequest request = new SyncRequest();
    request.setRepo("newRepo");
    request.setBranch("master");
    request.setSha("good_key");
    CommandContext<SyncRequest> ctx = new CommandContext<SyncRequest>(new IpAddress(4321), new Message<SyncRequest>(ProtocolMessage.SYNC, request));
    
    assertTrue("Command failed to execute", cmd.execute(ctx));
    
    assertTrue("Message sent", sender.dest == null && sender.msg == null);
    assertTrue("Maint Filter didn't behave as expected", maintFilter.didStart && maintFilter.didStop);
    assertTrue("Content didn't switch", fileServlet.switched);
    assertTrue("Listener not returned to original", worker.listener == listener);    
    
  }
}
