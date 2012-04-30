package com.meltmedia.cadmium.demos.basic;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.meltmedia.cadmium.servlets.FileServlet;
import com.meltmedia.cadmium.servlets.MaintenanceFilter;

/**
 * Builds the context with the Guice framework.  To see how this works, go to:
 * http://code.google.com/p/google-guice/wiki/ServletModule
 * 
 * @author Christian Trimble
 */

public class DemoListener extends GuiceServletContextListener {
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
          fileParams.put("basePath", "/Library/WebServer/Cadmium");
          
          serve("/").with(FileServlet.class, fileParams);
		}
      };
	}
}
