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

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.ApiEndpointAccessController;
import com.meltmedia.cadmium.core.config.ConfigManager;

/**
 * Servlet filter to restrict access to certain paths managed by the {@link ApiEndpointAccessController}.
 * 
 * @author John McEntire
 *
 */
@Singleton
public class ApiEndpointAccessFilter implements Filter {
  public static final String PERSISTED_STATE_CONFIG_KEY = "com.meltmedia.cadmium.servlets.ApiEndpointAccessFilter.disabled.paths";
  
  @Inject
  protected ConfigManager configManager;
  
  /**
   * The internal implementation of the {@link ApiEndpointAccessController} interface that is used by this class.
   * 
   * @author John McEntire
   *
   */
  private static final class ApiEndpointAccessControllerImplementation implements ApiEndpointAccessController {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    protected ConfigManager configManager;
    
    private Set<String> paths = new TreeSet<String>();

    @Override
    public void enable(String endpoint) {
      paths.remove(endpoint);
      updateDefaultProperties();
    }

    @Override
    public void disable(String endpoint) {
      paths.add(endpoint);
      updateDefaultProperties();
    }

    @Override
    public String[] getDisabled() {
      return paths.toArray(new String []{});
    }
    
    public void updateDefaultProperties() {
      configManager.getDefaultProperties().setProperty(PERSISTED_STATE_CONFIG_KEY, new Gson().toJson(paths));
      configManager.persistDefaultProperties();
    }
    
    /**
     * Initializes the state of this with the persisted state.
     * 
     * @param configManager
     */
    @Inject
    public void setConfigManager(ConfigManager configManager) {
      this.configManager = configManager;
      try {
        String pathsStr = configManager.getDefaultProperties().getProperty(PERSISTED_STATE_CONFIG_KEY, "[]");
        String paths[] = new Gson().fromJson(pathsStr, String[].class);
        ((ApiEndpointAccessControllerImplementation)controller).paths.clear();
        ((ApiEndpointAccessControllerImplementation)controller).paths.addAll(Arrays.asList(paths));
      } catch(Exception e) {
        log.warn("Failed to initialize state from persisted properties.", e);
      }
    }
    
  }
  
  /**
   * The instance of the ApiEndpointAccessController currently being used by this filter.
   */
  public static final ApiEndpointAccessController controller = new ApiEndpointAccessControllerImplementation();
  
  private String prefix;

  @Override
  public void init(FilterConfig config) throws ServletException {
    if(config.getInitParameter("jersey-prefix") != null) {
      prefix = config.getInitParameter("jersey-prefix");
    }
    if(StringUtils.isEmptyOrNull(prefix)){
      prefix = "/api";
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    String pathRequested = ((HttpServletRequest)request).getRequestURI();
    if(pathRequested.startsWith(prefix)) {
      pathRequested = pathRequested.replaceFirst(Pattern.quote(prefix), "");
      for(String disabled : controller.getDisabled()) {
        if(disabled.equals(pathRequested)) {
          ((HttpServletResponse)response).sendError(HttpStatus.SC_NOT_FOUND);
          return;
        } 
      }
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {}

}
