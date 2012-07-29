package com.meltmedia.cadmium.servlets;

import javax.servlet.http.HttpServletRequest;

/**
 * A SecureRedirectStrategy for use when the servlet container is doing ssl termination.
 * 
 * @author Christian Trimble
 */
public class SimpleSecureRedirectStrategy extends AbstractSecureRedirectStrategy {

  /**
   * Returns the protocol from the request.
   */
  @Override
  public String getProtocol(HttpServletRequest request) {
    return request.getProtocol();
  }

  /**
   * Returns the server port from the request.
   */
  @Override
  public int getPort(HttpServletRequest request) {
    return request.getServerPort();
  }
  
  /**
   * Returns true if the request to the servlet container is secure, false otherwise.
   */
  @Override
  public boolean isSecure(HttpServletRequest request) {
    return request.isSecure();
  }

}
