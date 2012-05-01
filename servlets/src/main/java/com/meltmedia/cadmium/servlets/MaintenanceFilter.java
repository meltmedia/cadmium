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

	public volatile boolean isOn = true;
	

	@Override
	public void init(FilterConfig config)
			throws ServletException {

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		if( !isOn ) {
			chain.doFilter(req, res);
			return;
		}
		HttpServletResponse httpRes = (HttpServletResponse)res;
		HttpServletRequest httpReq = (HttpServletRequest)req;
		
		httpRes.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		PrintWriter writer = httpRes.getWriter();
		writer.append("<html><body><div>This server is under maintenance.</div></body></html>");
		writer.close();
	}
	
	public void start()
	{
		isOn = true;
	}
	
	public void stop() {
		isOn = false;
	}

}
