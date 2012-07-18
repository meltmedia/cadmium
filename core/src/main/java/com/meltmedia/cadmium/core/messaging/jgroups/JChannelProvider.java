/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.messaging.jgroups;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A provider for the JGroups channel for this cluster.
 * 
 * @author John McEntire
 */
@Singleton
public class JChannelProvider implements Provider<JChannel>, Closeable {
  
  public static final String CHANNEL_NAME = "JGroupsName";
  public static final String CONFIG_NAME = "JGroupsConfigName";
  
  private final Logger log = LoggerFactory.getLogger(getClass());

  private String channelName;
  private URL configFile;
  private JChannel channel;
  
  @Inject
  public JChannelProvider(@Named(CHANNEL_NAME) String channelName, @Named(CONFIG_NAME) URL configFile) {
    this.channelName = channelName;
    this.configFile = configFile;
  }
  
  @Override
  public JChannel get() {
    if(channel == null) {
      try{
        channel = new JChannel(configFile);
        channel.connect(channelName);
      } catch(Exception e) {
        log.error("Failed to get jgroups channel", e);
      }
    }
    return channel;
  }

  @Override
  public void close() throws IOException {
    if(channel != null) {
      channel.close();
    }
  }

}
