package com.meltmedia.cadmium.servlets;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.meltmedia.cadmium.core.meta.SslRedirectConfigProcessor;

@Singleton
public class SslRedirectFilter implements Filter {
  
  public final static String SSL_HEADER_NAME = "SSLHeaderName";
  
  @Inject
  protected SslRedirectConfigProcessor redirect;
  
  @Inject(optional=true)
  @Named(SSL_HEADER_NAME)
  protected String sSLHeaderName = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {
    if(redirect != null && req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
      HttpServletRequest request = (HttpServletRequest)req;
      HttpServletResponse response = (HttpServletResponse)resp;
      if(!request.isSecure() || sslByHeader(request)) {
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
  
  private boolean sslByHeader(HttpServletRequest req) {
    if(sSLHeaderName != null) {
      if(req.getHeader(sSLHeaderName) != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void destroy() {

  }

}
