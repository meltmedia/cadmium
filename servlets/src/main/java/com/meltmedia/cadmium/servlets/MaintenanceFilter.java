package com.meltmedia.cadmium.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.meltmedia.cadmium.core.SiteDownService;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MaintenanceFilter extends HttpFilter implements Filter, SiteDownService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

	public volatile boolean on = false;
	private String ignorePath;


	@Override
	public void init(FilterConfig config)
	throws ServletException {
		if(config.getInitParameter("ignorePrefix") != null) {
			ignorePath = config.getInitParameter("ignorePrefix");
		}
		config.getServletContext().setAttribute(this.getClass().getName(), this);
	}

	@Override
	public void destroy() {

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
      logger.debug("Serving request server:{}, uri:{}", httpReq.getServerName(), uri);
			chain.doFilter(httpReq, httpRes);
			return;
		}

		httpRes.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		InputStream in = null;
		try {
		  in = MaintenanceFilter.class.getResourceAsStream("./maintenance.html");
		  InputStreamReader reader = new InputStreamReader(in, "UTF-8");
		  IOUtils.copy(reader, httpRes.getWriter()); 
		}
		finally {
		  IOUtils.closeQuietly(in);
		  IOUtils.closeQuietly(httpRes.getWriter());
		}
	}

	@Override
	public void start()	{		
		on = true;
	}

	@Override
	public void stop() {
		on = false;
	}

	public boolean isOn() {
		return on;
	}


}
