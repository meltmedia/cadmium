package com.meltmedia.cadmium.demos.basic;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;

import org.jgroups.JChannel;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.meltmedia.cadmium.jgit.impl.CoordinatedWorkerImpl;
import com.meltmedia.cadmium.jgroups.JChannelProvider;
import com.meltmedia.cadmium.jgroups.receivers.UpdateChannelReceiver;
import com.meltmedia.cadmium.servlets.FileServlet;
import com.meltmedia.cadmium.servlets.MaintenanceFilter;

/**
 * Builds the context with the Guice framework.  To see how this works, go to:
 * http://code.google.com/p/google-guice/wiki/ServletModule
 * 
 * @author Christian Trimble
 */

public class DemoListener extends GuiceServletContextListener {
  private static final String applicationBasePath = "/Library/WebServer/Cadmium";
	Injector injector = null;

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(createServletModule());
	}
	
	private ServletModule createServletModule() {
      return new ServletModule() {
        @Override
		protected void configureServlets() {
          
          bind(MaintenanceFilter.class).in(Scopes.SINGLETON);
          
          bind(FileServlet.class).in(Scopes.SINGLETON);
          
          filter("/*").through(MaintenanceFilter.class);
          
          Map<String, String> fileParams = new HashMap<String, String>();
          fileParams.put("basePath", applicationBasePath);
          
          serve("/").with(FileServlet.class, fileParams);
          
          //Bind application base path
          bind(String.class).annotatedWith(Names.named(UpdateChannelReceiver.BASE_PATH)).toInstance(applicationBasePath);
          
          //Bind channel name
          bind(String.class).annotatedWith(Names.named(JChannelProvider.CHANNEL_NAME)).toInstance("CadmiumChannel");
          
          //Bind Config file URL
          URL propsUrl = JChannelProvider.class.getClassLoader().getResource("tcp.xml");
          bind(URL.class).annotatedWith(Names.named(JChannelProvider.CONFIG_NAME)).toInstance(propsUrl);
          
          //Bind JChannel provider
          bind(JChannel.class).toProvider(JChannelProvider.class);
          
          //Bind CoordinatedWorker
          bind(CoordinatedWorkerImpl.class).in(Scopes.SINGLETON);          
          
          //Bind UpdateChannelReceiver
          bind(UpdateChannelReceiver.class).in(Scopes.SINGLETON);
          
		}
      };
	}
}
