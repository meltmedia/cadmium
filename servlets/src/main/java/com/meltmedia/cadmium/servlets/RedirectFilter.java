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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.meta.Redirect;
import com.meltmedia.cadmium.core.meta.RedirectConfigProcessor;

@Singleton
public class RedirectFilter implements Filter {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected RedirectConfigProcessor redirect;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {
    if(redirect != null && req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
      HttpServletRequest request = (HttpServletRequest)req;
      HttpServletResponse response = (HttpServletResponse)resp;
      String path = request.getRequestURI();
      String queryString = request.getQueryString();
      log.debug("Checking for existing redirect [{}?{}]", path, queryString);
      Redirect redir = redirect.requestMatches(path, queryString);
      if(redir != null) {
        String redirectTo = redir.getUrlSubstituted();
        response.setHeader("Location", redirectTo);
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        return;
      }
    } else {
      log.info("Redirect and/or req and resp are not http");
    }
    chain.doFilter(req, resp);
  }

  @Override
  public void destroy() {

  }

}
