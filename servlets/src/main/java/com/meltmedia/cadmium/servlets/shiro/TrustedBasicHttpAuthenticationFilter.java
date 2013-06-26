package com.meltmedia.cadmium.servlets.shiro;

import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * com.meltmedia.cadmium.servlets.shiro.TrustedBasicHttpAuthenticationFilter
 *
 * @author jmcentire
 */
public class TrustedBasicHttpAuthenticationFilter extends BasicHttpAuthenticationFilter {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected List<String> trustedHosts = new ArrayList<String>();

  public List<String> getTrustedHosts() {
    return trustedHosts;
  }

  public void setTrustedHosts(List<String> trustedHosts) {
    this.trustedHosts = trustedHosts;
  }

  @Override
  public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
    logger.debug("Checking {} trusted hosts: {}",request.getRemoteAddr(), trustedHosts);
    if(!CollectionUtils.isEmpty(trustedHosts) && trustedHosts.contains(request.getRemoteAddr())) {
      logger.debug("Skipping auth because host is trusted.");
      return true;
    }
    return super.onPreHandle(request, response, mappedValue);
  }
}
