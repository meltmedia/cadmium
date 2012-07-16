package com.meltmedia.cadmium.core.messaging.jgroups;

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
public class JChannelProvider implements Provider<JChannel> {
  
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

}
