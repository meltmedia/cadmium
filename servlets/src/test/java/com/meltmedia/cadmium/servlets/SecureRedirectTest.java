package com.meltmedia.cadmium.servlets;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.meltmedia.cadmium.core.meta.SslRedirectConfigProcessor;
import static com.meltmedia.cadmium.servlets.AbstractSecureRedirectStrategy.getDefaultPort;

/**
 * Tests the SslRedirectFilter, making sure that it properly redirects users for both secure and insecure urls.
 * 
 * @author Christian Trimble
 */
@RunWith(Parameterized.class)
public class SecureRedirectTest {
  public static String TEXT_HTML = "text/html";
  public static String TEXT_HTML_UTF_8 = "text/html; charset=utf-8";
  public static String TEXT_CSS = "text/css";
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        // the filter must redirect pages when SSL termination is done by the container.
        { simpleRequest("http://domain.com/secure.html"), TEXT_HTML, true, "https://domain.com/secure.html" },
        { simpleRequest("http://domain.com/insecure.html"), TEXT_HTML, false, null },
        { simpleRequest("https://domain.com/secure.html"), TEXT_HTML, false, null },
        { simpleRequest("https://domain.com/insecure.html"), TEXT_HTML, true, "http://domain.com/insecure.html" },
        { simpleRequest("http://domain.com:8080/secure.html"), TEXT_HTML, true, "https://domain.com:8443/secure.html" },
        { simpleRequest("http://domain.com:8080/insecure.html"), TEXT_HTML, false, null },
        { simpleRequest("https://domain.com:8433/secure.html"), TEXT_HTML, false, null },
        { simpleRequest("https://domain.com:8443/insecure.html"), TEXT_HTML, true, "http://domain.com:8080/insecure.html" },
        
        // missing files should not redirect.
        { simpleRequest("https://domain.com/missing.html"), null, false, null },
        { simpleRequest("https://domain.com/missing.html"), null, false, null },
        { simpleRequest("https://domain.com:8080/missing.html"), null, false, null },
        { simpleRequest("https://domain.com:8443/missing.html"), null, false, null },
        
        // only html files should redirect.
        { simpleRequest("http://domain.com/site.css"), TEXT_CSS, false, null },
        { simpleRequest("https://domain.com/site.css"), TEXT_CSS, false, null },
        
        // the content type test should work even if character set information is present.
        { simpleRequest("http://domain.com/secure.html"), TEXT_HTML_UTF_8, true, "https://domain.com/secure.html" },
        { simpleRequest("http://domain.com/insecure.html"), TEXT_HTML_UTF_8, false, null },
        { simpleRequest("https://domain.com/secure.html"), TEXT_HTML_UTF_8, false, null },
        { simpleRequest("https://domain.com/insecure.html"), TEXT_HTML_UTF_8, true, "http://domain.com/insecure.html" },        

        // the filter must redirect pages when SSL termination is done by a load balancer.
        { proxiedRequest("http", 80, "http://domain.com:8080/secure.html"), TEXT_HTML, true, "https://domain.com/secure.html" },
        { proxiedRequest("http", 80, "http://domain.com:8080/insecure.html"), TEXT_HTML, false, null },
        { proxiedRequest("https", 443, "http://domain.com:8080/secure.html"), TEXT_HTML, false, null },
        { proxiedRequest("https", 443, "http://domain.com:8080/insecure.html"), TEXT_HTML, true, "http://domain.com/insecure.html" },
        
        // the filter must not effect things under /api or /service
        { simpleRequest("https://domain.com:8443/api/service"), null, false, null },
        { simpleRequest("http://domain.com:8080/api/service"), null, false, null },
        { simpleRequest("https://domain.com:8443/system/service"), null, false, null },
        { simpleRequest("http://domain.com:8080/system/service"), null, false, null },
        { proxiedRequest("https", 443, "http://domain.com:8080/system/service"), null, false, null },
        { proxiedRequest("http", 80, "http://domain.com:8080/system/service"), null, false, null },
        { proxiedRequest("https", 443, "http://domain.com:8080/api/service"), null, false, null },
        { proxiedRequest("http", 80, "http://domain.com:8080/api/service"), null, false, null }
    });
  }
  
  /**
   * Creates a mock HttpServletRequest simulating SSL termination by the container.
   * 
   * @param requestUriString the request URL to mock.
   * @return the mocked HttpServletRequest
   */
  public static HttpServletRequest simpleRequest(String requestUriString) {
    URI requestUri = URI.create(requestUriString);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader(anyString())).thenReturn(null);
    when(request.getRequestURL()).thenReturn(new StringBuffer(requestUri.toString()));
    when(request.isSecure()).thenReturn("https".equals(requestUri.getScheme()));
    when(request.getServerPort()).thenReturn(requestUri.getPort() != -1 ? requestUri.getPort() : getDefaultPort(requestUri.getScheme()));
    when(request.getProtocol()).thenReturn(requestUri.getScheme());
    return request;
  }
  
  /**
   * Creates a mock HttpServletRequest simulating SSL termination by a load balancer, using X-Forwarded headers.
   * 
   * @param requestProto the protocol recieved by the proxy
   * @param requestPort the port on the proxy
   * @param requestUriString the request made by the proxy
   * @return the mocked HttpServletRequest
   */
  public static HttpServletRequest proxiedRequest(String requestProto, int requestPort, String requestUriString) {
    URI requestUri = URI.create(requestUriString);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader(XForwardedSecureRedirectStrategy.X_FORWARED_PROTO)).thenReturn(requestProto);
    when(request.getHeader(XForwardedSecureRedirectStrategy.X_FORWARED_PORT)).thenReturn(Integer.toString(requestPort));
    when(request.getRequestURL()).thenReturn(new StringBuffer(requestUri.toString()));
    when(request.isSecure()).thenReturn("https".equals(requestUri.getScheme()));
    when(request.getServerPort()).thenReturn(requestUri.getPort() != -1 ? requestUri.getPort() : getDefaultPort(requestUri.getScheme()));
    when(request.getProtocol()).thenReturn(requestUri.getScheme());
    return request;
  }
  
  SecureRedirectFilter filter;
  String contentType;
  HttpServletRequest request;
  private boolean redirect;
  private String expectedLocation;
  
  public SecureRedirectTest( HttpServletRequest request, String contentType, boolean redirect, String expectedLocation ) {
    this.request = request;
    this.contentType = contentType;
    this.redirect = redirect;
    this.expectedLocation = expectedLocation;
  }
  
  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws IOException {
    SslRedirectConfigProcessor redirect = mock(SslRedirectConfigProcessor.class);
    when(redirect.shouldBeSsl("/secure.html")).thenReturn(true);
    when(redirect.shouldBeSsl("/insecure.html")).thenReturn(false);
    
    FileServlet fileServlet = mock(FileServlet.class);
    if( contentType != null ) {
      when(fileServlet.contentTypeOf(anyString())).thenReturn(contentType);
    }
    else {
      when(fileServlet.contentTypeOf(anyString())).thenThrow(FileNotFoundException.class);
    }

    filter = new SecureRedirectFilter();
    filter.setRedirectStrategy(new XForwardedSecureRedirectStrategy());
    filter.setRedirectConfigProcessor(redirect);
    filter.setFileServlet(fileServlet);
  }
  
  @After
  public void tearDown() {
    
  }

  /**
   * Runs the supplied request object and verifies that it was properly handled by the SecureRedirectFilter.
   * 
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testRedirect() throws IOException, ServletException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    ServletOutputStream out = mock(ServletOutputStream.class);
    when(response.getOutputStream()).thenReturn(out);
    FilterChain chain = mock(FilterChain.class);
    
    filter.doFilter(request, response, chain);
    
    if( redirect ) {
      // verify the status and the location header.
      verify(response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
      verify(response).setHeader("Location", expectedLocation);
      verify(out).flush();
      verify(out).close();
      
      // verify that the chain was not called.
      verifyZeroInteractions(chain);
    }
    else {
      // verify that the response was not changed.
      verifyZeroInteractions(response);
      
      // verify that the doFilter method was called.
      verify(chain).doFilter(request, response);
    }
    
    
  }
  
  public static Map<String, String> map( String... keyValue ) {
    if( keyValue.length % 2 != 0 ) throw new IllegalArgumentException("Could not create map, there is an odd number of arguments.");
    Map<String, String> map = new HashMap<String, String>();
    for( int i = 0; i < keyValue.length; i+=2 ) {
      map.put(keyValue[i], keyValue[i+1]);
    }
    return map;
  }
}
