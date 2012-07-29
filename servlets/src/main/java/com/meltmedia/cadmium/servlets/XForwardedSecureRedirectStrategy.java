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

import javax.servlet.http.HttpServletRequest;

/**
 * A redirect strategy that uses the X-Forwarded-Proto header to determine
 * the protocol that was originally used with the request.
 * 
 * @author Christian Trimble
 */
public class XForwardedSecureRedirectStrategy extends SimpleSecureRedirectStrategy {

  /** The original protocol header injected by proxies. */
  public static final String X_FORWARED_PROTO = "X-Forwarded-Proto";
  
  /** The original port header injected by proxies. */
  public static final String X_FORWARED_PORT = "X-Forwarded-Port";
  
  /**
   * When the X-Forwarded-Proto header is present, this method returns true if the header equals "https", false otherwise.  When
   * the header is not present, it delegates this call to the super class.
   */
  @Override
  public boolean isSecure(HttpServletRequest request) {
    String proto = request.getHeader(X_FORWARED_PROTO);
    if( proto != null ) return proto.equalsIgnoreCase(HTTPS_PROTOCOL);
    return super.isSecure(request);
  }
  
  /**
   * When the X-Forwarded-Proto header is present, this method returns the value of that header.  When
   * the header is not present, it delegates this call to the super class.
   */
  @Override
  public String getProtocol(HttpServletRequest request) {
    String proto = request.getHeader(X_FORWARED_PROTO);
    if( proto != null ) return proto;
    return super.getProtocol(request);
  }
  
  /**
   * When the X-Forwarded-Port header is present, this method returns the value of that header.  When
   * the header is not present, it delegates this call to the super class.
   */
  @Override
  public int getPort(HttpServletRequest request) {
    String portValue = request.getHeader(X_FORWARED_PORT);
    if( portValue != null ) return Integer.parseInt(portValue);
    return super.getPort(request);
  }

}
