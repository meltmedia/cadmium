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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPOutputStream;

public class BasicFileServlet
  extends HttpServlet
{
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private static final long serialVersionUID = 1L;
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
  public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
  public static final String CONTENT_TYPE_HEADER = "Content-Type";
  public static final String TEXT_HTML_TYPE = "text/html";
  public static final String LOCATION_HEADER = "Location";
  public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
  public static final String ACCEPT_RANGES_HEADER = "Accept-Ranges";
  public static final String CONTENT_RANGE_HEADER = "Content-Range";
  public static final String CONTENT_LENGTH_HEADER = "Content-Length";
  public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
  public static final String ACCEPT_HEADER = "Accept";
  
  public static final String RANGE_BOUNDARY = "RANGES_BOUNDARY_";
  
  protected File contentDir = null;
  protected Long lastUpdated = System.currentTimeMillis();
  
  protected List<String> gzipList = new ArrayList<String>();
  
  public BasicFileServlet() {
    super();
    gzipList.addAll(Arrays.asList(new String [] {"text/*", "application/javascript", "application/x-javascript", "application/json", "application/xml", "application/xslt+xml"}));
  }
  
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    if(this.contentDir == null) {
      setBasePath(config.getInitParameter("basePath"));
    }
  }
  
  protected void setLastUpdated(long lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  protected void setBasePath(String basePath) throws ServletException {
    if(basePath == null) {
      throw new ServletException("Please set the base path in init parameter \"basePath\".");
    } else {
      File contentDir = new File(basePath);
      if(!contentDir.exists()) {
        throw new ServletException("The basePath \""+basePath+"\" does not exist on the file system.");
      } else if(!contentDir.isDirectory()) {
        throw new ServletException("The basePath \""+basePath+"\" exists and is not a directory.");
      } else if(!contentDir.canRead()) {
        throw new ServletException("The basePath\""+basePath+"\" cannot be read.");
      }
      this.contentDir = contentDir;
    }
  }
  
  protected String getBasePath() {
    return contentDir.toString();
  }
  
  
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequest(new FileRequestContext(req, resp, true));
  }
  
  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    processRequest(new FileRequestContext(req, resp, false));
  }
  
  public void processRequest( FileRequestContext context )
    throws ServletException, IOException {
    try {
      // Find the file to serve in the file system.  This may redirect for welcome files or send 404 responses.
      if(locateFileToServe(context)) return;

      // Handle any conditional headers that may be present.
      if(handleConditions(context)) return;

      // Sets the content type header.
      resolveContentType(context);

      // Sets compress if Accept-Encoding allows for gzip or identity
      if(checkAccepts(context)) return;

      try {
        if(context.file != null) {
          context.response.setHeader(CONTENT_DISPOSITION_HEADER, "inline;filename=\"" + context.file.getName() + "\"");
        }
        context.response.setHeader(ACCEPT_RANGES_HEADER, "bytes");
        if(context.eTag != null) {
          context.response.setHeader(ETAG_HEADER, context.eTag);
        }
        context.response.setDateHeader(LAST_MODIFIED_HEADER, lastUpdated);
        if(!context.ranges.isEmpty()) {
          context.response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
          String rangeBoundary = RANGE_BOUNDARY + UUID.randomUUID().toString();
          if(context.ranges.size() > 1) {
            context.response.setContentType("multipart/byteranges; boundary=" + rangeBoundary);
            context.response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

            if(context.sendEntity) {
              context.in = new FileInputStream(context.file);
              context.out = context.response.getOutputStream();
              ServletOutputStream sout = (ServletOutputStream)context.out;
              for(Range r : context.ranges) {
                sout.println();
                sout.println("--"+rangeBoundary);
                sout.println("Content-Type: " + context.contentType);
                sout.println("Context-Range: bytes " + r.start + "-" + r.end + "/" + context.file.length());

                copyPartialContent(context.in, context.out, r);
              }

              sout.println();
              sout.println("--"+rangeBoundary+"--");
            }
          } else {
            Range r = context.ranges.get(0);
            context.response.setContentType(context.contentType);
            Long rangeLength = calculateRangeLength(context, r);
            context.response.setHeader(CONTENT_RANGE_HEADER, "bytes " + r.start + "-" + r.end
                + "/" + context.file.length());
            if(context.sendEntity) {
              context.in = new FileInputStream(context.file);
              context.out = context.response.getOutputStream();

              context.response.setHeader(CONTENT_LENGTH_HEADER, rangeLength.toString());

              copyPartialContent(context.in, context.out, r);
            }
          }
        } else {
          context.response.setStatus(HttpServletResponse.SC_OK);
          context.response.setContentType(context.contentType);

          if( context.sendEntity ) {
            context.response.setHeader(CONTENT_RANGE_HEADER, "bytes 0-" + context.file.length() + "/" + context.file.length());
            context.in = new FileInputStream(context.file);
            context.out = context.response.getOutputStream();
            if( context.compress ) {

              context.response.setHeader(CONTENT_ENCODING_HEADER, "gzip");
              context.out = new GZIPOutputStream(context.out);

            } else context.response.setHeader(CONTENT_LENGTH_HEADER, new Long(context.file.length()).toString());

            IOUtils.copy(context.in, context.out);
          }
        }
      }
      finally {
        IOUtils.closeQuietly(context.in);
        IOUtils.closeQuietly(context.out);
      }
    } catch(IOException ioe) {
      logger.error("Received an IOException serving request: " + context.path, ioe);
      throw ioe;
    } catch(Throwable t) {
      logger.error("Failed to serve request: " + context.path, t);
      throw new ServletException(t);
    }
  }
  
  /**
   * Copies the given range of bytes from the input stream to the output stream.
   * 
   * @param in
   * @param out
   * @param r
   * @throws IOException 
   */
  public static void copyPartialContent(InputStream in, OutputStream out, Range r) throws IOException {
    IOUtils.copyLarge(in, out, r.start, r.length);
  }

  /**
   * Calculates the length of a given range.
   * 
   * @param context
   * @param range
   * @return
   */
  public static Long calculateRangeLength(FileRequestContext context, Range range) {
    if(range.start == -1) range.start = 0;
    if(range.end == -1) range.end = context.file.length() - 1;
    range.length = range.end - range.start + 1;
    return range.length;
  }

  /**
   * Checks the accepts headers and makes sure that we can fulfill the request.
   * @param context 
   * @return
   * @throws IOException 
   */
  protected boolean checkAccepts(FileRequestContext context) throws IOException {
    if (!canAccept(context.request.getHeader(ACCEPT_HEADER), false, context.contentType)) {
      notAcceptable(context);
      return true;
    }
    
    if (!(canAccept(context.request.getHeader(ACCEPT_ENCODING_HEADER), false, "identity") || canAccept(context.request.getHeader(ACCEPT_ENCODING_HEADER), false, "gzip"))) {
      notAcceptable(context);
      return true;
    }
    
    if (context.request.getHeader(ACCEPT_ENCODING_HEADER) != null && canAccept(context.request.getHeader(ACCEPT_ENCODING_HEADER), true, "gzip") && shouldGzip(context.contentType)) {
      context.compress = true;
    }
    return false;
  }

  /**
   * <p>Checks for inclusion in the gzipList field. Allows basic wild cards.</p>
   * <p>An empty or null field defaults to gzipping of all content types.</p> 
   * @param contentType
   * @return
   */
  public boolean shouldGzip(String contentType) {
    if(gzipList == null || gzipList.isEmpty()) {
      return true;
    } else {
      return gzipList.contains(contentType) || gzipList.contains(contentType.replaceAll("/[^/]+\\Z", "/*")) || gzipList.contains(contentType.replaceAll("\\A[^/]+/", "*/"));
    }
  }


  /**
   * Locates the file to serve.  Returns true if locating the file caused the request to be handled.
   * @param context
   * @return
   * @throws IOException
   */
  public boolean locateFileToServe( FileRequestContext context ) throws IOException {
    context.file = new File( contentDir, context.path);
    
    // if the path is not on the file system, send a 404.
    if( !context.file.exists() ) {
      context.response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;    
    }
    
    // redirect welcome files if needed.
    if( handleWelcomeRedirect(context) ) return true;
    
    // if the requested file is a directory, try to find the welcome file.
    if( context.file.isDirectory() ) {
      context.file = new File(context.file, "index.html");
    }
    
    // if the file does not exist, then terminate with a 404.
    if( !context.file.exists() ) {
      context.response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }
    
    return false;
  }
  
  public boolean handleConditions( FileRequestContext context ) throws IOException {
    // check the conditions
    context.eTag = context.path+"_"+lastUpdated;
    
    context.ifMatch = context.request.getHeader(IF_MATCH_HEADER);
    if( context.ifMatch != null && !validateStrong(context.ifMatch, context.eTag) ) {
      context.response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      return true;
    }
    
    context.range = context.request.getHeader(RANGE_HEADER);
    
    try {
      context.inRangeDate = context.request.getDateHeader(IF_RANGE_HEADER);
      if(context.inRangeDate != -1 && context.inRangeDate >= lastUpdated) {
        if(!parseRanges(context)){
          invalidRanges(context);
          return true;
        }
      }
    } catch(IllegalArgumentException iae) {
      logger.error("Invalid range serving request: "+context.path, iae);
      context.inRangeETag = context.request.getHeader(IF_RANGE_HEADER);
      if(context.inRangeETag != null && validateStrong(context.inRangeETag, context.eTag ) ) {
        if(!parseRanges(context)){
          invalidRanges(context);
          return true;
        }
      }
    }
    
    if(context.inRangeDate == -1 && context.inRangeETag == null) {
      if(!parseRanges(context)){
        invalidRanges(context);
        return true;
      }
    }
    
    context.ifNoneMatch = context.request.getHeader(IF_NONE_MATCH_HEADER);
    if( context.ifNoneMatch != null && validateStrong(context.ifNoneMatch, context.eTag)) {
      context.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      context.response.setHeader(ETAG_HEADER, context.eTag);
      context.response.setDateHeader(LAST_MODIFIED_HEADER, lastUpdated);
      return true;
    }
    
    context.ifModifiedSince = context.request.getDateHeader(IF_MODIFIED_SINCE_HEADER);
    if( context.ifModifiedSince != -1 && context.ifModifiedSince >= lastUpdated ) {
      context.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      context.response.setHeader(ETAG_HEADER, context.eTag);
      context.response.setDateHeader(LAST_MODIFIED_HEADER, lastUpdated);
      return true;
    }
    
    context.ifUnmodifiedSince = context.request.getDateHeader(IF_UNMODIFIED_SINCE_HEADER);
    if( context.ifUnmodifiedSince != -1 && context.ifUnmodifiedSince < lastUpdated ) {
      context.response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      return true;
    }
    
    return false;

  }

  /**
   * Sets the appropriate response headers and error status for bad ranges
   * 
   * @param context
   * @throws IOException
   */
  public static void invalidRanges(FileRequestContext context) throws IOException {
    context.response.setHeader(CONTENT_RANGE_HEADER, "*/" + context.file.length());
    context.response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
  }
  
  public static Pattern etagPattern = null;
  public static Pattern unescapePattern = null;
  public static Pattern rangePattern = null;
  static {
    try {
      etagPattern = Pattern.compile( "(W/)?\"((?:[^\"\\\\]|\\\\.)*)\"\\s*(?:,\\s*)?");
      unescapePattern = Pattern.compile("\\\\(.)");
      rangePattern = Pattern.compile("\\A\\s*bytes\\s*=\\s*(\\d*-\\d*(,\\d*-\\d*)*)\\s*\\Z");
    }
    catch( PatternSyntaxException pse ) {
      pse.printStackTrace();
    }
  }
  
  /**
   * Parses the Range Header of the request and sets the appropriate ranges list on the {@link FileRequestContext} Object.
   * 
   * @param context
   * @return false if the range pattern contains no satisfiable ranges.
   */
  public static boolean parseRanges(FileRequestContext context) {
    if( !StringUtils.isBlank(context.range) ) {
      Matcher rangeMatcher = rangePattern.matcher(context.range);
      if(rangeMatcher.matches()) {
        String ranges[] = rangeMatcher.group(1).split(",");
        for(String range : ranges) {
          long startBound = -1;
          int hyphenIndex = range.indexOf('-');
          if( hyphenIndex > 0 ) {
            startBound = Long.parseLong(range.substring(0, hyphenIndex));
          }
          long endBound = -1;
          if( hyphenIndex >= 0 && (hyphenIndex + 1) < range.length() ) {
            endBound = Long.parseLong(range.substring(hyphenIndex + 1));
          }
          Range newRange = new Range(startBound, endBound);
          
          if(!(startBound != -1 && endBound != -1 && startBound > endBound) && !(startBound == -1 && endBound == -1)) {
            context.ranges.add(newRange);
          }
        } 
        return !context.ranges.isEmpty();
      }
    }
    return true;
  }
  
  /**
   * Parses an Accept header value and checks to see if the type is acceptable.
   * @param headerValue The value of the header.
   * @param strict Forces the type to be in the header.
   * @param type The token that we need in order to be acceptable.
   * @return
   */
  public static boolean canAccept(String headerValue, boolean strict, String type) {
    if(headerValue != null && type != null)  {
      String availableTypes[] = headerValue.split(",");
      for(String availableType : availableTypes) {
        String typeParams[] = availableType.split(";");
        double qValue = 1.0d;
        if(typeParams.length > 0) {
          for(int i=1; i<typeParams.length; i++) {
            if(typeParams[i].trim().startsWith("q=")) {
              String qString = typeParams[i].substring(2).trim();
              if(qString.matches("\\A\\d+(\\.\\d*){0,1}\\Z")){
                qValue = Double.parseDouble(qString);
                break;
              } 
            }
          }
        }
        boolean matches = false;
        if(typeParams[0].equals("*") || typeParams[0].equals("*/*")) {
          matches = true;
        } else {
          matches = hasMatch(typeParams, type);
        }
        if(qValue == 0 && matches) {
          return false;
        } else if(matches){
          return true;
        }
      }
    }
    return !strict;
  }

  /**
   * Check to see if a Accept header accept part matches any of the given types.
   * @param typeParams
   * @param type
   * @return
   */
  private static boolean hasMatch(String[] typeParams, String... type) {
    boolean matches = false;
    for(String t : type) {
      for(String typeParam : typeParams) {
        if(typeParam.contains("/")) {
          String typePart = typeParam.replace("*", "");
          if(t.startsWith(typePart) || t.endsWith(typePart)) {
            matches = true;
            break;
          }
        } else if(t.equals(typeParam)) {
          matches = true;
          break;
        }
      }
      if(matches) {
        break;
      }
    }
    return matches;
  }
  
  /**
   * Sends an error on the response for status code 406 NOT ACCEPTABLE.
   * @param context
   * @throws IOException
   */
  public static void notAcceptable(FileRequestContext context) throws IOException {
    context.response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
  }
  
  public static List<String> parseETagList( String value ) {
    List<String> etags = new ArrayList<String>();
    value = value.trim();
    if( "*".equals(value) ) {
      etags.add(value);
    }
    else {
      Matcher etagMatcher = etagPattern.matcher(value);
      while( etagMatcher.lookingAt() ) {
        etags.add(unescapePattern.matcher(etagMatcher.group(2)).replaceAll("$1"));
        etagMatcher.region(etagMatcher.start()+etagMatcher.group().length(), value.length());
      }
      if(!etagMatcher.hitEnd()) {
        etags.clear();
      }
    }
    return etags;
  }
  
  public static boolean validateStrong( String condition, String eTag ) {
    List<String> parsed = parseETagList(condition);
    if( parsed.size() == 1 && "*".equals(parsed.get(0)) ) return true;
    else return parsed.contains(eTag);
  }
  
  /**
   * Forces requests for index files to not use the file name.
   * 
   * @param context
   * @return true if the request was handled, false otherwise.
   * @throws IOException 
   */
  public boolean handleWelcomeRedirect( FileRequestContext context ) throws IOException {
    if( context.file.isFile() && context.file.getName().equals("index.html")) {
      resolveContentType(context);
      String location = context.path.replaceFirst("/index.html\\Z", "");
      if( location.isEmpty() ) location = "/";
      if( context.request.getQueryString() != null ) location = location + "?" + context.request.getQueryString();
      sendPermanentRedirect(context, location);
      return true;
    }
    return false;
  }

  /**
   * Looks up the mime type based on file extension and if found sets it on the FileRequestContext.
   * 
   * @param context
   */
  public void resolveContentType(FileRequestContext context) {
    String contentType = lookupMimeType(context.file.getName());
    if(contentType != null) {
      context.contentType = contentType;
      if(contentType.equals("text/html")) {
        context.contentType += ";charset=UTF-8";
      }
    }
  }
  
  public static void sendPermanentRedirect( FileRequestContext context, String location ) throws IOException {
    context.response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    context.response.setContentType(context.contentType);
    context.response.setHeader(LOCATION_HEADER, location);
    context.response.getOutputStream().close();
  }
  
  public String lookupMimeType( String path ) {
    return getServletContext().getMimeType(path);
  }
  
  public static class FileRequestContext
  {
    public String range;
    public List<Range> ranges = new ArrayList<Range>();
    public long inRangeDate = -1;
    public String inRangeETag;
    public long ifUnmodifiedSince;
    public long ifModifiedSince;
    public String ifNoneMatch;
    public String eTag;
    HttpServletRequest request = null;
    HttpServletResponse response = null;
    File file = null;
    InputStream in = null;
    OutputStream out = null;
    boolean compress = false;
    String path = null;
    private boolean sendEntity = true;
    String contentType = DEFAULT_CONTENT_TYPE;
    public String ifMatch;
    
    public FileRequestContext( HttpServletRequest request, HttpServletResponse response, boolean sendEntity ) {
      this.request = request;
      this.response = response;
      this.sendEntity = sendEntity;
      this.path = request.getRequestURI();
    }
  }
  
  public static class Range {
    public long start = -1l;
    public long end = -1l;
    public long length;
    public Range(long start, long end) {
      this.start = start;
      this.end = end;
    }
  }
}
