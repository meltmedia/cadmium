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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceInputStream;

import com.meltmedia.cadmium.core.ContentService;

import static org.mockito.Mockito.*;

/**
 * Tests the selection of the proper error page by the ErrorPageFilter servlet.
 * 
 * @author Christian Trimble
 */
@RunWith(Parameterized.class)
public class ErrorPageFilterSelectionTest {
  
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { sendErrorFilterChain(404), "404" },
        { sendErrorFilterChain(407), "40x" },
        { sendErrorFilterChain(412), "4xx" },
        { sendErrorFilterChain(501), "501" },
        { sendErrorFilterChain(502), "50x" },
        { sendErrorFilterChain(510), "5xx" },
        { throwExceptionFilterChain(new Exception("Oops!")), "50x"},
        { successFilterChain("Success"), "Success"} });
  }
  
  /**
   * Creates a filter chain that calls sendError(int) with the specified status code.
   * 
   * @param statusCode the status code to send.
   * @return a filter chain that simulates a sendError condition.
   */
  public static FilterChain sendErrorFilterChain( final int statusCode ) {
    return new FilterChain() {
      @Override
      public void doFilter(ServletRequest req, ServletResponse res)
          throws IOException, ServletException {
        ((HttpServletResponse)res).sendError(statusCode);
      }
    };
  }
  
  /**
   * Creates a filter chain that throws a ServletException with the specified cause.
   * @param cause the cause of the exception that will be thrown.
   * @return a filter chain that simulates an exception.
   */
  public static FilterChain throwExceptionFilterChain( final Throwable cause ) {
    return new FilterChain() {
      @Override
      public void doFilter(ServletRequest req, ServletResponse res)
          throws IOException, ServletException {
        throw new ServletException(cause);
      }
    }; 
  }
  
  /**
   * Creates a filter chain that sends a 200 response.
   * 
   * @param content the content to write to the res object.
   * @return a filter chain that simulates a 200 response.
   */
  public static FilterChain successFilterChain( final String content ) {
    return new FilterChain() {
      @Override
      public void doFilter(ServletRequest req, ServletResponse res)
          throws IOException, ServletException {
        HttpServletResponse httpRes = (HttpServletResponse)res;
        httpRes.setStatus(200);
        Writer out = null;
        Reader in = null;
        try {
          out = res.getWriter();
          in = new StringReader(content);
          IOUtils.copy(in, out);
        }
        finally {
          IOUtils.closeQuietly(out);
          IOUtils.closeQuietly(in);
        }
      }
    };    
  }

  /**
   * The filter chain to execute inside the filter.
   */
  private FilterChain chain;
  
  /**
   * The content we expect to be written to the response writer.
   */
  private String expectedContent;
  
  /**
   * The filter that will be tested.
   */
  private ErrorPageFilter filter;
  
  /**
   * Creates a new test object from the parameters.
   * 
   * @param chain the chain that will be called in the filter.
   * @param expectedContent the expected content on the response writer.
   */
  public ErrorPageFilterSelectionTest( FilterChain chain, String expectedContent ) {
    this.chain = chain;
    this.expectedContent = expectedContent;
  }
  
  /**
   * Mock up the content service for the error pages and create the filter.
   * 
   * @throws ServletException if there is a problem initializing the filter.
   * @throws IOException if there is a problem mocking the content service.
   */
  @Before
  public void beforeTest() throws IOException, ServletException {
    ContentService contentService = mock(ContentService.class);
    when(contentService.getResourceAsStream("/404.html")).thenReturn(new CharSequenceInputStream("404", "UTF-8"));
    when(contentService.getResourceAsStream("/407.html")).thenReturn(null);
    when(contentService.getResourceAsStream("/412.html")).thenReturn(null);
    when(contentService.getResourceAsStream("/40x.html")).thenReturn(new CharSequenceInputStream("40x", "UTF-8"));
    when(contentService.getResourceAsStream("/41x.html")).thenReturn(null);
    when(contentService.getResourceAsStream("/4xx.html")).thenReturn(new CharSequenceInputStream("4xx", "UTF-8"));
    when(contentService.getResourceAsStream("/501.html")).thenReturn(new CharSequenceInputStream("501", "UTF-8"));
    when(contentService.getResourceAsStream("/502.html")).thenReturn(null);
    when(contentService.getResourceAsStream("/510.html")).thenReturn(null);
    when(contentService.getResourceAsStream("/50x.html")).thenReturn(new CharSequenceInputStream("50x", "UTF-8"));
    when(contentService.getResourceAsStream("/51x.html")).thenReturn(null);
    when(contentService.getResourceAsStream("/5xx.html")).thenReturn(new CharSequenceInputStream("5xx", "UTF-8"));
    
    filter = new ErrorPageFilter();
    filter.setContentService(contentService);
    
    filter.init(mock(FilterConfig.class));
    
  }

  /**
   * Clean up the filter that was tested.
   */
  @After
  public void afterTest() {
    filter.destroy();
  }
  
  /**
   * Calls doFilter on the ErrorPageFilter, capturing the output.  The output is compared to the expected output.
   * 
   * @throws IOException if there is an error in the test.
   * @throws ServletException if there is an error in the test.
   */
  @Test
  public void testFilterChainError() throws IOException, ServletException {
    StringWriter resultWriter = new StringWriter();
    
    // Return the result writer for output.
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(new PrintWriter(resultWriter));
    
    // act
    filter.doFilter(mock(HttpServletRequest.class), response, chain);
    
    // assert
    assertEquals("The wrong error content was returned.", expectedContent, resultWriter.toString());
  }
}
