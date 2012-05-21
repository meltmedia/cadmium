package com.meltmedia.cadmium.core.meta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Redirect implements Cloneable {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private String url;
  private String path;
  private Pattern pathPattern;
  
  private volatile Matcher matcher;
  
  public Redirect(){}

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getPath() {
    return path;
  }
  
  public String getUrlSubstituted() {
    if(matcher != null) {
      return matcher.group().replaceAll(path, url);
    }
    return url;
  }
  
  public Pattern getPathPattern() {
    return pathPattern;
  }

  public void setPath(String path) {
    this.path = path;
    try {
      pathPattern = null;
      pathPattern = Pattern.compile(path);
      log.debug("Parsed as pattern {}", path);
    } catch(Throwable t) {
      log.debug("This is not a REGEX pattern", t);
    }
  }
  
  public boolean matches(String pathInfo) {
    if(pathPattern != null) {
      log.debug("Using pattern to match {}", pathPattern.pattern());
      matcher = pathPattern.matcher(pathInfo);
      boolean matched = matcher.matches();
      if(!matched) {
        matcher = null;
      }
      return matched;
    } else if(pathInfo.equals(path)){
      log.debug("Path equals pathinfo {}", pathInfo);
      return true;
    }
    return false;
  }
  
  @Override
  public Object clone() {
    Redirect cloneOf = new Redirect();
    cloneOf.url = url;
    if(this.pathPattern == null) {
      cloneOf.setPath(path);
    } else {
      cloneOf.path = path;
      cloneOf.pathPattern = pathPattern;
    }
    cloneOf.matcher = matcher;
    return cloneOf;
  }
  
}
