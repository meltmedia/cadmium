package com.meltmedia.cadmium.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.meltmedia.cadmium.jgroups.SiteDownService;

public class MaintenanceFilter implements Filter, SiteDownService {

	public volatile boolean isOn = false;
	private String ignorePath;
	

	@Override
	public void init(FilterConfig config)
			throws ServletException {
	  if(config.getInitParameter("ignorePrefix") != null) {
	    ignorePath = config.getInitParameter("ignorePrefix");
	  }
	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
    HttpServletResponse httpRes = (HttpServletResponse)res;
    HttpServletRequest httpReq = (HttpServletRequest)req;
    String contextPath = httpReq.getContextPath();
    String uri = httpReq.getRequestURI();
    if(contextPath != null && contextPath.trim().length() > 0 && uri.startsWith(contextPath)) {
      uri = uri.substring(contextPath.length());
    }
    if( !isOn || (ignorePath != null && uri.startsWith(ignorePath)) ) {
			chain.doFilter(req, res);
			return;
		}
		
		httpRes.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		PrintWriter writer = httpRes.getWriter();
		writer.append("<html><body><div>This server is under maintenance.</div></body></html>");
		writer.close();
	}
	
	@Override
	public void start()
	{
		isOn = true;
	}
	
	@Override
	public void stop() {
		isOn = false;
	}


}
