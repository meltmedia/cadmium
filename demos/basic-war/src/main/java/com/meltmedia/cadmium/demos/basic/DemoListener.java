package com.meltmedia.cadmium.demos.basic;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String CONFIG_PROPERTIES_FILE = "config.properties";
  public static final String BASE_PATH_ENV = "com.meltmedia.cadmium.contentRoot";
  public static final String REPO_KEY_ENV = "com.meltmedia.cadmium.github.sshKey";
  public static final String LAST_UPDATED_DIR = "com.meltmedia.cadmium.lastUpdated";
  private String applicationBasePath = "/Library/WebServer/Cadmium";
  private String repoDir = "git-checkout";
  private String contentDir = "renderedContent";
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
	  Properties configProperties = new Properties();
    configProperties.putAll(System.getenv());
    configProperties.putAll(System.getProperties());
    
    if(configProperties.containsKey(BASE_PATH_ENV) ) {
      File basePath = new File(configProperties.getProperty(BASE_PATH_ENV));
      if(basePath.exists() && basePath.canRead() && basePath.canWrite()) {
        this.applicationBasePath = basePath.getAbsolutePath();
      }
    }
	  String repoDir = servletContextEvent.getServletContext().getInitParameter("repoDir");
	  if(repoDir != null && repoDir.trim().length() > 0) {
	    this.repoDir = repoDir;
	  }
	  String contentDir = servletContextEvent.getServletContext().getInitParameter("contentDir");
	  if(contentDir != null && contentDir.trim().length() > 0) {
	    this.contentDir = contentDir;
	  }
    File repoFile = new File(this.applicationBasePath, this.repoDir);
    if(repoFile.isDirectory() && repoFile.canWrite()) {
      this.repoDir = repoFile.getAbsoluteFile().getAbsolutePath();
    }
    File contentFile = new File(this.applicationBasePath, this.contentDir);
    if(contentFile.isDirectory() && contentFile.canWrite()) {
      this.contentDir = contentFile.getAbsoluteFile().getAbsolutePath();
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
          
          Properties configProperties = new Properties();
          configProperties.putAll(System.getenv());
          configProperties.putAll(System.getProperties());
          
          try{
            configProperties.load(new FileReader(new File(applicationBasePath, CONFIG_PROPERTIES_FILE)));
          } catch(Exception e){
            log.warn("Failed to load properties file ["+CONFIG_PROPERTIES_FILE+"] from content directory.", e);
          }
          
          if(configProperties.containsKey(LAST_UPDATED_DIR)) {
            File cntDir = new File(configProperties.getProperty(LAST_UPDATED_DIR));
            if(cntDir.exists() && cntDir.canRead()) {
              contentDir = cntDir.getAbsolutePath();
            }
          }

          bind(MaintenanceFilter.class).in(Scopes.SINGLETON);
          bind(SiteDownService.class).to(MaintenanceFilter.class);
          
          bind(FileServlet.class).in(Scopes.SINGLETON);
          bind(ContentService.class).to(FileServlet.class);
          
          Map<String, String> fileParams = new HashMap<String, String>();
          fileParams.put("basePath", contentDir);
          
          Map<String, String> maintParams = new HashMap<String, String>();
          maintParams.put("ignorePrefix", "/system");

          serve("/system/*").with(GuiceContainer.class);
          
          serve("/*").with(FileServlet.class, fileParams);
          
          filter("/*").through(MaintenanceFilter.class, maintParams);

          //Bind application base path
          bind(String.class).annotatedWith(Names.named(UpdateChannelReceiver.BASE_PATH)).toInstance(applicationBasePath);
                    
          //Bind git repo path
          bind(String.class).annotatedWith(Names.named(UpdateService.REPOSITORY_LOCATION)).toInstance(repoDir);
          
          //Bind static content path
          bind(String.class).annotatedWith(Names.named(CoordinatedWorkerImpl.RENDERED_DIRECTORY)).toInstance(contentDir);
          
          //Bind channel name
          bind(String.class).annotatedWith(Names.named(JChannelProvider.CHANNEL_NAME)).toInstance("CadmiumChannel");
          
          if(configProperties.containsKey(REPO_KEY_ENV)) {
            bind(String.class).annotatedWith(Names.named(REPO_KEY_ENV)).toInstance(configProperties.getProperty(REPO_KEY_ENV));
          }
          
          bind(Properties.class).annotatedWith(Names.named(CONFIG_PROPERTIES_FILE)).toInstance(configProperties);
          
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
		}
      };
	}
}
