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
package com.meltmedia.cadmium.servlets;

import com.meltmedia.cadmium.core.SiteDownService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Singleton
public class MaintenanceFilter extends HttpFilter implements Filter {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  final static class MaintSiteDownService implements SiteDownService
  {
    
    protected MaintenanceFilter filter = null;
    protected boolean active = false;
    
    synchronized void setMaintenanceFilter( MaintenanceFilter filter ) {
      this.filter = filter;
      if( filter != null ) {
        if( active ) filter.start();
        else filter.stop();
      }
    }

    @Override
    public synchronized void start() {
      active = true;
      if( filter != null ) filter.start();
    }

    @Override
    public synchronized void stop() {
      active = false;
      if( filter != null ) filter.stop();
    }

    @Override
    public synchronized boolean isOn() {
      return active;
    }
  }
  
  public static final SiteDownService siteDown = new MaintSiteDownService();

	public volatile boolean on = true;
	private String ignorePath;


	@Override
	public void init(FilterConfig config)
	throws ServletException {
		if(config.getInitParameter("ignorePrefix") != null) {
			ignorePath = config.getInitParameter("ignorePrefix");
		}
		config.getServletContext().setAttribute(this.getClass().getName(), this);
		((MaintSiteDownService) siteDown).setMaintenanceFilter(this);
	}

	@Override
	public void destroy() {
    ((MaintSiteDownService) siteDown).setMaintenanceFilter(null);
	}

	@Override
	public void doFilter(HttpServletRequest httpReq, HttpServletResponse httpRes, FilterChain chain)
	throws IOException, ServletException {
		String contextPath = httpReq.getContextPath();
		String uri = httpReq.getRequestURI();
		if(contextPath != null && contextPath.trim().length() > 0 && uri.startsWith(contextPath)) {
			uri = uri.substring(contextPath.length());
		}
		if( !on || (ignorePath != null && uri.startsWith(ignorePath)) ) {
      logger.trace("Serving request server:{}, uri:{}", httpReq.getServerName(), uri);
			chain.doFilter(httpReq, httpRes);
			return;
		}

		httpRes.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		httpRes.setContentType(MediaType.TEXT_HTML);
		InputStream in = null;
    InputStreamReader reader = null;
		try {
		  in = MaintenanceFilter.class.getResourceAsStream("/maintenance.html");
		  if(in == null) {
		    in = MaintenanceFilter.class.getResourceAsStream("./maintenance.html");
		  }
		  reader = new InputStreamReader(in, "UTF-8");
		  IOUtils.copy(reader, httpRes.getWriter()); 
		}
		finally {
      IOUtils.closeQuietly(reader);
		  IOUtils.closeQuietly(in);
		  IOUtils.closeQuietly(httpRes.getWriter());
		}
	}

	public void start()	{		
		on = true;
	}

	public void stop() {
		on = false;
	}

	public boolean isOn() {
		return on;
	}


}
