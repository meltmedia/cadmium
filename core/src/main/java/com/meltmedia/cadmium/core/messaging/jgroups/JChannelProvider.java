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

import com.meltmedia.cadmium.core.messaging.MessagingChannelName;
import com.meltmedia.cadmium.core.messaging.MessagingConfigurationUrl;
import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

/**
 * A provider for the JGroups channel for this cluster.
 * 
 * @author John McEntire
 */
@Singleton
public class JChannelProvider implements Provider<JChannel>, Closeable {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

  private String channelName;
  private URL configFile;
  private JChannel channel;
  
  @Inject
  public JChannelProvider(@MessagingChannelName String channelName, @MessagingConfigurationUrl URL configFile) {
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
      try {
        channel.close();
      } catch(Throwable t){
        log.error("Failed to close jgroups channel.", t);
      }
      channel = null;
    }
  }

}
