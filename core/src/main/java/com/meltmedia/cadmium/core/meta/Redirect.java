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
package com.meltmedia.cadmium.core.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
      log.trace("Parsed as pattern {}", path);
    } catch(Throwable t) {
      log.trace("This is not a REGEX pattern", t);
    }
  }
  
  public boolean matches(String pathInfo) {
    if(pathPattern != null) {
      log.trace("Using pattern to match {}", pathPattern.pattern());
      matcher = pathPattern.matcher(pathInfo);
      if(matcher != null) {
        boolean matched = matcher.matches();
        if(!matched) {
          matcher = null;
        }
        return matched;
      } else {
        log.warn("Found null matcher [{}] on redir [{}].", pathInfo, path);
      }
    } else if(pathInfo.equals(path)){
      log.trace("Path equals pathinfo {}", pathInfo);
      return true;
    }
    return false;
  }
  
  @Override
  public Object clone() {
    Redirect cloneOf = new Redirect();
    cloneOf.url = url;
    cloneOf.path = path;
    if(this.pathPattern != null) {
      cloneOf.pathPattern = pathPattern;
    }
    cloneOf.matcher = matcher;
    return cloneOf;
  }
  
}
