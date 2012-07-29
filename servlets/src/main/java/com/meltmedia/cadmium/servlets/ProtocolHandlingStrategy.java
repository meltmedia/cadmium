package com.meltmedia.cadmium.servlets;

import javax.servlet.http.HttpServletRequest;

public interface ProtocolHandlingStrategy {
  public SecureRedirectStrategy protocolSecurityHandler(HttpServletRequest request);
}
