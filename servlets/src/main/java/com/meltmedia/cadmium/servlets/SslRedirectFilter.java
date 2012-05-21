package com.meltmedia.cadmium.servlets;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.meltmedia.cadmium.core.meta.SslRedirectConfigProcessor;

@Singleton
public class SslRedirectFilter implements Filter {
  
  @Inject
  protected SslRedirectConfigProcessor redirect;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {
    if(redirect != null && req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
      HttpServletRequest request = (HttpServletRequest)req;
      HttpServletResponse response = (HttpServletResponse)resp;
      if(!request.isSecure()) {
        String path = request.getRequestURI();
        if(redirect.shouldBeSsl(path)) {
          String redirectTo = "https://" 
              + request.getServerName()  
              + path 
              + (request.getQueryString() != null && request.getQueryString().length() > 0 ? "?" + request.getQueryString() : "");
          response.setHeader("Location", redirectTo);
          response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
          return;
        }
      }
    }
    chain.doFilter(req, resp);
  }

  @Override
  public void destroy() {

  }

}
