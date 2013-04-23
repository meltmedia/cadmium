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

import com.meltmedia.cadmium.core.CadmiumFilter;
import com.meltmedia.cadmium.core.meta.AlternateContentConfigProcessor;
import com.meltmedia.cadmium.core.meta.MimeTypeConfigProcessor;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Filter that will handle multiple alternate content directories based on "User-Agent" headers.
 *
 * @author John McEntire
 */
@CadmiumFilter("/*")
@Singleton
public class UserAgentAlternateContentFilter implements Filter {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The config processor for the alternate-content.json file.
   */
  @Inject
  protected AlternateContentConfigProcessor alternateContentConfig;

  /**
   * The mime type config processor.
   */
  @Inject
  protected MimeTypeConfigProcessor mimeTypes;

  /**
   * Ignores that default Jersey mapped filters. <em>"/api", "/system"</em>
   */
  protected static final List<String> IGNORE_PATH_PREFIXES = Arrays.asList("/api", "/system");

  /**
   * Allowed request methods. <em>"GET", "HEAD"</em>
   */
  protected static final List<String> ALLOWED_METHODS = Arrays.asList("GET","HEAD");

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    try {
      if(req instanceof HttpServletRequest){
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        if(ALLOWED_METHODS.contains(request.getMethod().toUpperCase())) {
          String requestUri = request.getRequestURI();
          String userAgentHeader = request.getHeader("User-Agent");
          if(!ignorePath(requestUri) && alternateContentConfig.hasConfig() && !StringUtils.isEmptyOrNull(userAgentHeader)) {
            File contentDir = alternateContentConfig.getAlternateContentDirectory(userAgentHeader);
            if(contentDir != null && contentDir.exists() && contentDir.isDirectory()) {
              try {
                logger.debug("ContentDir {}, Serving {}, servlet path {}, path info {}", new Object[]{contentDir, request.getRequestURI(), request.getServletPath(), request.getPathInfo()});
                FileServlet fileServlet = new FileServlet();
                fileServlet.init();
                fileServlet.setBasePath(contentDir.getAbsoluteFile().getAbsolutePath());
                fileServlet.service(request, response);
                return;
              } catch(Exception e) {
                logger.error("Failed to process request: "+requestUri, e);
              }
            }
          }
        }
      }
      chain.doFilter(req, resp);
    } catch (IOException ioe) {
      logger.error("Failed in user agent filter.", ioe);
      throw ioe;
    } catch (ServletException se) {
      logger.error("Failed in user agent filter.", se);
      throw se;
    } catch (Throwable t) {
      logger.error("Failed in user agent filter.", t);
      throw new ServletException(t);
    }
  }

  @Override
  public void destroy() {}

  private boolean ignorePath(String path) {
    for(String prefix: IGNORE_PATH_PREFIXES) {
      if(path.toLowerCase().startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  public class FileServlet extends BasicFileServlet {
    @Override
    public String lookupMimeType(String filename) {
      if (mimeTypes == null) throw new RuntimeException("The mime type processor is not set!.");
      return mimeTypes.getContentType(filename);
    }

    public String getContentRoot() {
      return getBasePath();
    }

    /**
     * Returns the content type for the specified path.
     *
     * @param path the path to look up.
     * @return the content type for the path.
     * @throws java.io.FileNotFoundException if a file (or welcome file) does not exist at path.
     * @throws IOException                   if any other problem prevents the lookup of the content type.
     */
    public String contentTypeOf(String path) throws IOException {
      File file = findFile(path);
      return lookupMimeType(file.getName());
    }

    /**
     * Returns the file object for the given path, including welcome file lookup.  If the file cannot be found, a
     * FileNotFoundException is returned.
     *
     * @param path the path to look up.
     * @return the file object for that path.
     * @throws java.io.FileNotFoundException if the file could not be found.
     * @throws IOException                   if any other problem prevented the locating of the file.
     */
    public File findFile(String path) throws FileNotFoundException, IOException {
      File base = new File(getBasePath());
      File pathFile = new File(base, "." + path);
      if (!pathFile.exists()) throw new FileNotFoundException("No file or directory at " + pathFile.getCanonicalPath());
      if (pathFile.isFile()) return pathFile;
      pathFile = new File(pathFile, "index.html");
      if (!pathFile.exists()) throw new FileNotFoundException("No welcome file at " + pathFile.getCanonicalPath());
      return pathFile;
    }
  }
}
