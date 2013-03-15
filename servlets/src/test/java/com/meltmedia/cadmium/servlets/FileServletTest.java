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

import com.meltmedia.cadmium.core.meta.MimeTypeConfigProcessor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class FileServletTest {
  
  public static final Long CURRENT_LAST_MODIFIED = new Long(327758400);
  public static final Long PAST_LAST_MODIFIED = new Long(233409600);
  public static final String CURRENT_INDEX_ETAG = "/_"+CURRENT_LAST_MODIFIED.toString();
  public static final String PAST_INDEX_ETAG = "/_"+PAST_LAST_MODIFIED.toString();
  public static final String CURRENT_SECTION_INDEX_ETAG = "/section_"+CURRENT_LAST_MODIFIED.toString();
  public static final String PAST_SECTION_INDEX_ETAG = "/section_"+PAST_LAST_MODIFIED.toString();
  
  public static final String GET_METHOD = "GET";
  public static final String HEAD_METHOD = "HEAD";
  public static final String ETAG_HEADER = "ETag";
  public static final String IF_MATCH_HEADER = "If-Match";
  public static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
  public static final String IF_UNMODIFIED_SINCE_HEADER = "If-Unmodified-Since";
  public static final String IF_NONE_MATCH_HEADER = "If-None-Match";
  public static final String LAST_MODIFIED_HEADER = "Last-Modified";
  public static final String RANGE_HEADER = "Range";
  public static final String IF_RANGE_HEADER = "If-Range";
  public static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
  public static final String CONTENT_TYPE_HEADER = "Content-Type";
  public static final String TEXT_HTML_TYPE = "text/html";
  public static final String LOCATION_HEADER = "Location";
  public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
  public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
  public static final String ACCEPT_RANGES_HEADER = "Accept-Ranges";
  public static final String CONTENT_RANGE_HEADER = "Content-Range";
  public static final String CONTENT_LENGTH_HEADER = "Content-Length";
  public static File CONTENT_ROOT = null;
  public static byte[] INDEX_BYTES = null;
  
  static {
    try {
      CONTENT_ROOT = new File("./src/test/resources/test-content");
      INDEX_BYTES = fileBytes(new File(CONTENT_ROOT, "index.html"));
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }
  
  static FileServlet fileServlet;
  
  @BeforeClass
  public static void beforeClass() throws ServletException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, FileNotFoundException, IOException {
    MimeTypeConfigProcessor mimeTypes = mock(MimeTypeConfigProcessor.class);
    when(mimeTypes.getContentType(anyString())).thenReturn("text/html");
    
    ServletConfig config = mock(ServletConfig.class);
    when(config.getInitParameter("basePath")).thenReturn(CONTENT_ROOT.getAbsolutePath());

    fileServlet = new FileServlet();
    fileServlet.setMimeTypeConfigProcessor(mimeTypes);
    fileServlet.setLastModifiedForTesting(CURRENT_LAST_MODIFIED);
    fileServlet.init(config);
    
  }
  
  public static byte[] fileBytes(File file) throws FileNotFoundException, IOException {
    FileInputStream in = null;
    try {
      return IOUtils.toByteArray(in = new FileInputStream(file));
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }
  
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        // Verify initial requests work.
        { mockGetWithGzip("/"), success("index.html", CURRENT_INDEX_ETAG, CURRENT_LAST_MODIFIED, true) },   
        { mockGet("/section"), success("index.html", CURRENT_SECTION_INDEX_ETAG, CURRENT_LAST_MODIFIED, false) },
        
        // verify that stale requests work.
        { mockGetWithIfNoneMatch("/", PAST_INDEX_ETAG), success("index.html", CURRENT_INDEX_ETAG, CURRENT_LAST_MODIFIED, false) },
        { mockGetWithIfModifiedSince("/", PAST_LAST_MODIFIED), success("index.html", CURRENT_INDEX_ETAG, CURRENT_LAST_MODIFIED, false) },
        
        // verify caching hits.
        { mockGetWithIfMatch("/", CURRENT_INDEX_ETAG), preconditionFailed() },
        { mockGetWithIfModifiedSince("/", CURRENT_LAST_MODIFIED), notModified(CURRENT_INDEX_ETAG) },
        
        // verify ranges
        { mockGetWithRanges("/", CURRENT_INDEX_ETAG, rangeSpec(10, 20, INDEX_BYTES)), success("index.html", CURRENT_INDEX_ETAG, CURRENT_LAST_MODIFIED, false, range(10, 20, INDEX_BYTES)) },
        
        // verify gzip
        
        // verify missing files.
        { mockGet("/unknown"), fileNotFound() },
        
        // verify welcome redirect.
        { mockGet("/index.html"), movedPerminately("/") },
        { mockGetWithIfNoneMatch("/index.html", CURRENT_INDEX_ETAG), movedPerminately("/") },
        { mockGet("/section/index.html"), movedPerminately("/section") },
        
        // verify precondition failed.
        { mockGetWithIfUnmodifiedSince("/", PAST_LAST_MODIFIED), preconditionFailed() },
        { mockGetWithoutGzip("/"), success("index.html", CURRENT_INDEX_ETAG, CURRENT_LAST_MODIFIED, false) }
    });
  }
  
  public static interface ResponseVerifier
  {
    public void verifyResponse(HttpServletResponse response) throws IOException, ServletException;
  }

  public HttpServletRequest request;
  public ResponseVerifier verifier;
  
  public FileServletTest(HttpServletRequest request, ResponseVerifier verifier) {
    this.request = request;
    this.verifier = verifier;
  }
  
  @Test
  public void test() throws IOException, ServletException {
    // mock the input and output.
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    HttpServletResponse response = mockResponse(output);
    
    // make the call.
    fileServlet.service(request, response);
    
    // check the results.
    verifier.verifyResponse(response);
  }
  
  public static void verifySuccess(HttpServletResponse response, String name, String etag, Long lastModified) {
    verify(response).setStatus(200);
    verify(response).setHeader(CONTENT_DISPOSITION_HEADER, "inline;filename=\""+name+"\"");
    verify(response).setHeader(ACCEPT_RANGES_HEADER, "bytes");
    verify(response).setHeader(ETAG_HEADER, etag);
    verify(response).setDateHeader(LAST_MODIFIED_HEADER, lastModified);
    verify(response).setHeader(eq(CONTENT_RANGE_HEADER), anyString());
    verify(response).setHeader(eq(CONTENT_LENGTH_HEADER), anyString());
    verify(response).setContentType(TEXT_HTML_TYPE+";charset=UTF-8");
  }
  

  public static HttpServletResponse mockResponse(OutputStream output) throws IOException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new MockServletOutputStream(output));
    return response;
  }
 
  public static HttpServletResponse mockResponse(final Map<String, List<String>> headers, OutputStream output) throws IOException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new MockServletOutputStream(output));
    if( headers != null ) {
    Answer<Void> addHeaderAnswer = new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        List<String> value = headers.get((String)args[0]);
        if( value == null ) headers.put((String)args[0], value = new ArrayList<String>());
        value.add(args[1].toString());
        return null;
      }
    };
    Answer<Void> setHeaderAnswer = new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        List<String> value = headers.get((String)args[0]);
        if( value == null ) headers.put((String)args[0], value = new ArrayList<String>());
        value.clear();
        value.add(args[1].toString());
        return null;
      }
    };
    doAnswer(setHeaderAnswer).when(response).setHeader(anyString(), anyString());
    doAnswer(setHeaderAnswer).when(response).setDateHeader(anyString(), anyLong());
    doAnswer(setHeaderAnswer).when(response).setIntHeader(anyString(), anyInt());
    doAnswer(addHeaderAnswer).when(response).addHeader(anyString(), anyString());
    doAnswer(addHeaderAnswer).when(response).addDateHeader(anyString(), anyLong());
    doAnswer(addHeaderAnswer).when(response).addIntHeader(anyString(), anyInt());
    }
    return response;
  }
  
  public static HttpServletRequest mockGet( String pathInfo ) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn(GET_METHOD);
    when(request.getPathInfo()).thenReturn(pathInfo);
    when(request.getDateHeader(anyString())).thenReturn(new Long(-1));
    when(request.getHeader(anyString())).thenReturn(null);
    return request;
  }
  
  public static HttpServletRequest mockGetWithGzip( String pathInfo ) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn(GET_METHOD);
    when(request.getPathInfo()).thenReturn(pathInfo);
    when(request.getDateHeader(anyString())).thenReturn(new Long(-1));
    when(request.getHeader(ACCEPT_ENCODING_HEADER)).thenReturn("gzip");
    return request;
  }
  
  public static HttpServletRequest mockGetWithoutGzip( String pathInfo ) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn(GET_METHOD);
    when(request.getPathInfo()).thenReturn(pathInfo);
    when(request.getDateHeader(anyString())).thenReturn(new Long(-1));
    when(request.getHeader(ACCEPT_ENCODING_HEADER)).thenReturn("gzip;q=0");
    return request;
  }
  
  public static HttpServletRequest mockGetWithRanges( String pathInfo, String eTag, final RangeSpec... rangeSpecs) {
    HttpServletRequest request = mockGet(pathInfo);
    when(request.getHeader(IF_RANGE_HEADER)).thenReturn(eTag);
    if( rangeSpecs.length > 0 ) {
      when(request.getHeader(RANGE_HEADER)).thenReturn(rangeHeader(rangeSpecs));
    }
    return request;
  }
  
  public static String rangeHeader(RangeSpec... rangeSpecs) {
    StringBuilder rangeBuilder = new StringBuilder().append("bytes=");
   for( int i = 0; i < rangeSpecs.length; i++ ) {
      rangeBuilder.append(rangeSpecs[i].start).append("-").append((rangeSpecs[i].end-1));
      if( i < rangeSpecs.length - 1) rangeBuilder.append(",");
    }
   return rangeBuilder.toString();
  }
  
  public static HttpServletRequest mockGetWithIfNoneMatch( String pathInfo, String eTag ) {
    HttpServletRequest request = mockGet(pathInfo);
    when(request.getHeader(IF_NONE_MATCH_HEADER)).thenReturn(eTag);
    return request;
  }
  
  public static HttpServletRequest mockGetWithIfMatch( String pathInfo, String eTag ) {
    HttpServletRequest request = mockGet(pathInfo);
    when(request.getHeader(IF_MATCH_HEADER)).thenReturn(eTag);
    return request;
  }
  
  public static HttpServletRequest mockGetWithIfModifiedSince( String pathInfo, Long lastModified ) {
    HttpServletRequest request = mockGet(pathInfo);
    when(request.getDateHeader(IF_MODIFIED_SINCE_HEADER)).thenReturn(lastModified);
    when(request.getHeader(anyString())).thenReturn(null);
    return request;
  }

  public static HttpServletRequest mockGetWithIfUnmodifiedSince( String pathInfo, Long lastModified ) {
    HttpServletRequest request = mockGet(pathInfo);
    when(request.getDateHeader(IF_UNMODIFIED_SINCE_HEADER)).thenReturn(lastModified);
    return request;
  }
  
  public static ResponseVerifier fileNotFound() {
    return new ResponseVerifier() {
      @Override
      public void verifyResponse(HttpServletResponse response) throws IOException, ServletException {
        verify(response).sendError(404);
      } 
    };
  }
  

  public static ResponseVerifier range( final int start, final int end, final byte[] bytes ) {
    return new ResponseVerifier() {
      @Override
      public void verifyResponse(HttpServletResponse response) throws IOException, ServletException {
        verify(response).setContentType(anyString());
        verify(response).setHeader(CONTENT_RANGE_HEADER, "bytes "+start+"-"+(end-1)+"/"+bytes.length);
      } 
    };
  }
  
  public static RangeSpec rangeSpec( int start, int end, byte[] bytes ) {
    return new RangeSpec( start, end, bytes.length );
  }
  
  public static class RangeSpec {
    public int start;
    public int end;
    public int total;
    public RangeSpec( int start, int end, int total ) {
      this.start = start;
      this.end = end;
      this.total = total;
    }
  }
  
  public static ResponseVerifier success(final String name, final String etag, final Long lastModified, final boolean shouldGzip, final ResponseVerifier... ranges) {
    return new ResponseVerifier() {
      @Override
      public void verifyResponse(HttpServletResponse response) throws IOException, ServletException {
        verify(response).setHeader(CONTENT_DISPOSITION_HEADER, "inline;filename=\""+name+"\"");
        verify(response).setHeader(ACCEPT_RANGES_HEADER, "bytes");
        verify(response).setHeader(ETAG_HEADER, etag);
        verify(response).setDateHeader(LAST_MODIFIED_HEADER, lastModified);
        if( ranges.length == 0 ) {
          verify(response).setStatus(200);
          verify(response).setHeader(eq(CONTENT_RANGE_HEADER), anyString());
          verify(response).setContentType(TEXT_HTML_TYPE+";charset=UTF-8");
          if(shouldGzip) {
            verify(response).setHeader(CONTENT_ENCODING_HEADER, "gzip");
          } else {
            verify(response).setHeader(eq(CONTENT_LENGTH_HEADER), anyString());
          }
        }
        else {
          for( ResponseVerifier range : ranges ) {
            range.verifyResponse(response);
          }
        }
      } 
    };
  }
  
  public static ResponseVerifier preconditionFailed() {
    return new ResponseVerifier() {
      @Override
      public void verifyResponse(HttpServletResponse response) throws IOException, ServletException {
        verify(response).sendError(412);
      } 
    };
  }
  
  public static ResponseVerifier movedPerminately(final String location) {
    return new ResponseVerifier() {
      @Override
      public void verifyResponse(HttpServletResponse response) throws IOException, ServletException {
        verify(response).setStatus(301);
        verify(response).setHeader(LOCATION_HEADER, location);
      } 
    };    
  }
  public static ResponseVerifier notModified(String etag) {
    return new ResponseVerifier() {
      @Override
      public void verifyResponse(HttpServletResponse response) throws IOException, ServletException {
        verify(response).setStatus(304);
        verify(response).setHeader(ETAG_HEADER, CURRENT_INDEX_ETAG);
      }
    };
  }
  
  public static class MockServletOutputStream
    extends ServletOutputStream
    {
    
    OutputStream wrapped;
    
    public MockServletOutputStream( OutputStream wrapped ) {
      this.wrapped = wrapped;
    }

    @Override
    public void write(int b) throws IOException {
      wrapped.write(b);
    }

    @Override
    public void close() throws IOException {
      super.close();
      wrapped.close();
    }

    @Override
    public void flush() throws IOException {
      super.flush();
      wrapped.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      wrapped.write(b, off, len);
    }
    
    }

}
