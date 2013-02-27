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


import com.meltmedia.cadmium.core.ContentService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * <p>Serves error content for calls to HttpServletResponse.sendError() methods and exceptions that are thrown during
 * response processing.</p>
 * <h3>Error Page Selection</h3>
 * <p>Error pages are selected by searching for a <STATUS_CODE>.html file in the ContentService.  Mare general error pages
 * are then searched for, by replacing least significant digits with the 'x' character in the status code.  For example, the
 * error page for a 404 response would search the following locations:</p>
 * <ol>
 *   <li>404.html</li>
 *   <li>40x.html</li>
 *   <li>4xx.html</li>
 * </ol>
 * <p>If no matching pages are found, then a default error page is returned.</p>
 * <h3>Thrown Exception</h3>
 * <p>This servlet catches java.lang.Throwable and renders a 500 error, to deal with exceptions that are propagating.</p>
 * <h3>Minimal Error Handling</h3>
 * <p>At a minimum, content being served by Cadmium should have a 4xx.html and a 5xx.html page defined.  That is a 'complete' error
 * handing setup.  Most likely, users will want to define a 404.html page, as these will be the most common type of error encountered.</p>
 * 
 * 
 * @author Brian Barr
 * @author Christian Trimble
 */
@Singleton
public class ErrorPageFilter implements Filter {
  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * The source of the error pages to server.
   */
  @Inject
  private ContentService contentService;
  
  /**
   * Sets the Content Service.  Used for testing.
   * 
   * @param contentService
   */
  void setContentService( ContentService contentService ) {
    this.contentService = contentService;
  }

  private String ignorePath;

  /**
   * Configures the ignore prefix.
   */
  @Override
  public void init(FilterConfig config)
      throws ServletException {
    if(config.getInitParameter("ignorePrefix") != null) {
      ignorePath = config.getInitParameter("ignorePrefix");
    }
  }

  /**
   * NoOp.
   */
  @Override
  public void destroy() {

  }

  /**
   * <p>Traps calls to sendError and catches all throwables from the chain.  Error pages from the content
   * service are rendered in these situations if:</p>
   * <ul>
   * <li>The status code is a 400 or 500.</li>
   * <li>The response has not been committed.</li>
   * </ul>
   * <p>Any thrown Throwable is treated like a 500 error.</p>
   * 
   * @param req the servlet request being handled.
   * @param res the servlet response being written to.
   * @param chain the filter chain to execute inside this filter.
   * @throws IOException if there is a problem rendering the error page.
   * @throws ServletException never.  
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest  httpReq = (HttpServletRequest)req;
    final HttpServletResponse httpRes = (HttpServletResponse)res;

    if( ignorePath != null ) {
      String contextPath = httpReq.getContextPath();
      String uri = httpReq.getRequestURI();
      if(contextPath != null && contextPath.trim().length() > 0 && uri.startsWith(contextPath)) {
        uri = uri.substring(contextPath.length());
      }
      if( uri.startsWith(ignorePath) ) {
        log.trace("Not handling errors for request server:{}, uri:{}", httpReq.getServerName(), uri);
        chain.doFilter(req, res);
        return;
      }
    }
    
    HttpServletResponse wrappedRes = new HttpServletResponseWrapper( httpRes ) {
      @Override
      public void sendError(int sc) throws IOException {
        ErrorPageFilter.this.sendError(sc, null, httpReq, httpRes);
      }

      @Override
      public void sendError(int sc, String msg) throws IOException {
        ErrorPageFilter.this.sendError(sc, msg, httpReq, httpRes);
      }
    };
    
    try {
      chain.doFilter(httpReq, wrappedRes);
    }
    catch( Throwable e ) {
      log.trace("Handling thrown Exception", e);
      sendError(500, null, httpReq, httpRes);
    }
  }
  
  /**
   * Called to render the error page, if possible.
   * 
   * @param sc the status code of the error.
   * @param message the message for the error.
   * @param request the request that caused the error.
   * @param response the response that the error will be written to.
   * @throws IOException if there is a problem rendering the error page.
   */
  @SuppressWarnings("deprecation")
  protected void sendError( int sc, String message, HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    if(400 <= sc && sc < 600 && !response.isCommitted()) {
      String[] fileNames = new String[] {
        "/"+Integer.toString(sc)+".html",
        "/"+Integer.toString(sc).subSequence(0, 2)+"x.html",
        "/"+Integer.toString(sc).subSequence(0, 1)+"xx.html" };
            
      InputStream errorPageIn = null;
      Reader errorPageReader = null;
      try {
        String path = request.getRequestURI();
        while(path != null && errorPageIn == null) {
          if(path.endsWith("/")) {
          	path = path.substring(0, path.length() - 1);
          }
	        for (String fileName : fileNames) {
	          if ((errorPageIn = contentService.getResourceAsStream(path + fileName)) != null) {
              log.debug("Found error page for path {} at {}", path, path + fileName);
	            break;
            }
	        }
          if(errorPageIn == null) {
            if(path.length() > 0) {
              path = path.substring(0, path.lastIndexOf("/"));
            } else {
              path = null;
            }
          }
        }

        // get the default page.
        if (errorPageIn == null) {
          for (String fileName : fileNames) {
            if((errorPageIn = ErrorPageFilter.class.getResourceAsStream(fileName)) != null) {
              log.debug("Found error page at {}", fileName);
              break;
            }
            else
              if ((errorPageIn = ErrorPageFilter.class.getResourceAsStream("./"+fileName)) != null) {
                log.debug("Found error page at {}", "./"+fileName);
                break;
              }
          }
        }
        
        if( errorPageIn == null ) {
          log.debug("No error page found.");
          if( message == null ) response.sendError(sc);
          else response.sendError(sc, message);
          return;
        }

        // set the status code.
        if (message != null)
          response.setStatus(sc, message);
        else
          response.setStatus(sc);

        // create a UTF-8 reader for the error page content.
        response.setContentType(MediaType.TEXT_HTML);
        log.trace("Sending error page content to response:{}", response.getClass().getName());
        IOUtils.copy(errorPageIn, response.getOutputStream());
        log.trace("Done sending error page. {}", sc);
      } finally {
        IOUtils.closeQuietly(errorPageIn);
        IOUtils.closeQuietly(response.getOutputStream());
      }
    }
    else {
      if( response.isCommitted() ) log.trace("Response is committed!");
      if( message == null ) response.sendError(sc);
      else response.sendError(sc, message);
    }
  }
}

