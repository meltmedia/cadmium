package com.meltmedia.cadmium.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A strategy for identifying the security of a request and changing the security of a request.
 * 
 * @author Christian Trimble
 *
 */
public interface SecureRedirectStrategy {

  /**
   * Returns true if the original request was made over a secure protocol, false otherwise.
   * @param request The request to test.
   * @return true if the original request was made over a secure protocol, false otherwise.
   */
  public boolean isSecure(HttpServletRequest request);
  
  /**
   * Sends the redirect required to change the request from insecure to secure.
   * 
   * @param request the insecure request that should be secure.
   * @param response the response to send redirect information to.
   * @throws IOException if there is a problem redirecting the user.
   */
  public void makeSecure(HttpServletRequest request, HttpServletResponse response) throws IOException;
  
  /**
   * Sends the redirect required to change the request from secure to insecure.
   * 
   * @param request the secure request that should be insecure.
   * @param response the response to send redirect information to.
   * @throws IOException if there is a problem redirecting the user.
   */
  public void makeInsecure(HttpServletRequest request, HttpServletResponse response) throws IOException;

}
