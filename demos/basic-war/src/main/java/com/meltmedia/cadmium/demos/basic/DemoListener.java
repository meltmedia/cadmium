package com.meltmedia.cadmium.demos.basic;

import java.io.File;
import java.io.IOException;
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
import com.meltmedia.cadmium.jgroups.ContentService;
import com.meltmedia.cadmium.jgroups.CoordinatedWorker;
import com.meltmedia.cadmium.jgroups.JChannelProvider;
import com.meltmedia.cadmium.jgroups.SiteDownService;
import com.meltmedia.cadmium.jgroups.jersey.UpdateService;
import com.meltmedia.cadmium.jgroups.receivers.UpdateChannelReceiver;
import com.meltmedia.cadmium.servlets.FileServlet;
import com.meltmedia.cadmium.servlets.MaintenanceFilter;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * Builds the context with the Guice framework.  To see how this works, go to:
 * http://code.google.com/p/google-guice/wiki/ServletModule
 * 
 * @author Christian Trimble
 */

public class DemoListener extends GuiceServletContextListener {
  private String applicationBasePath = "/Library/WebServer/Cadmium";
  private String repoDir = "git-checkout";
	Injector injector = null;

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
	  String applicationBasePath = servletContextEvent.getServletContext().getInitParameter("applicationBasePath");
	  if(applicationBasePath != null && applicationBasePath.trim().length() > 0) {
	    File appBasePath = new File(applicationBasePath);
	    if(appBasePath.isDirectory() && appBasePath.canWrite()) {
	      this.applicationBasePath = applicationBasePath;
	    }
	  }
	  String repoDir = servletContextEvent.getServletContext().getInitParameter("repoDir");
	  if(repoDir != null && repoDir.trim().length() > 0) {
	    this.repoDir = repoDir;
	  }
    File repoFile = new File(this.applicationBasePath, this.repoDir);
    if(repoFile.isDirectory() && repoFile.canWrite()) {
      this.repoDir = repoFile.getAbsoluteFile().getAbsolutePath();
    }
    super.contextInitialized(servletContextEvent);
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
          bind(SiteDownService.class).to(MaintenanceFilter.class);
          
          bind(FileServlet.class).in(Scopes.SINGLETON);
          bind(ContentService.class).to(FileServlet.class);
          
          filter("/*").through(MaintenanceFilter.class);
          
          Map<String, String> fileParams = new HashMap<String, String>();
          fileParams.put("basePath", repoDir);
          
          //serve("/*").with(FileServlet.class, fileParams);
          
          //Bind application base path
          bind(String.class).annotatedWith(Names.named(UpdateChannelReceiver.BASE_PATH)).toInstance(applicationBasePath);
                    
          //Bind git repo path
          bind(String.class).annotatedWith(Names.named(UpdateService.REPOSITORY_LOCATION)).toInstance(repoDir);
          
          //Bind channel name
          bind(String.class).annotatedWith(Names.named(JChannelProvider.CHANNEL_NAME)).toInstance("CadmiumChannel");
          
          //Bind Config file URL
          URL propsUrl = JChannelProvider.class.getClassLoader().getResource("tcp.xml");
          bind(URL.class).annotatedWith(Names.named(JChannelProvider.CONFIG_NAME)).toInstance(propsUrl);
          
          //Bind JChannel provider
          bind(JChannel.class).toProvider(JChannelProvider.class).in(Scopes.SINGLETON);
          
          //Bind CoordinatedWorker
          bind(CoordinatedWorker.class).to(CoordinatedWorkerImpl.class).in(Scopes.SINGLETON);          
          
          //Bind UpdateChannelReceiver
          bind(UpdateChannelReceiver.class).asEagerSingleton();
          
          //Bind Jersey UpdateService
          bind(UpdateService.class).in(Scopes.SINGLETON);

          serve("/system/*").with(GuiceContainer.class, new HashMap<String, String>());
		}
      };
	}
}
