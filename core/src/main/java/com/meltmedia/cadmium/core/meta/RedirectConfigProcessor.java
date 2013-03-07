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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class RedirectConfigProcessor implements ConfigProcessor {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String CONFIG_FILE_NAME = "redirect.json";
  
  protected List<Redirect> liveRedirects = new ArrayList<Redirect>();
  protected List<Redirect> stagedRedirects = new ArrayList<Redirect>();
  
  @Override
  public void processFromDirectory(String metaDir) throws Exception {
    synchronized(stagedRedirects) {
      if(metaDir != null) {
        String redirectFile = FileSystemManager.getFileIfCanRead(metaDir, CONFIG_FILE_NAME);
        if(redirectFile != null) {
          Gson gson = new Gson();
          Collection<Redirect> redirs = null;
          try {
            redirs = gson.fromJson(new FileReader(redirectFile), new TypeToken<Collection<Redirect>>(){}.getType());
          } catch(Exception e) {
            log.error("Invalid "+CONFIG_FILE_NAME+"!", e);
            throw e;
          }
          if(redirs != null && !redirs.isEmpty()) {
            stagedRedirects.clear();
            for(Redirect redir : redirs) {
              stagedRedirects.add((Redirect)redir.clone());
            }
          } else {
            stagedRedirects.clear();
          }
        } else {
          stagedRedirects.clear();
        }
      } else {
        stagedRedirects.clear();
      }
    }
  }

  @Override
  public void makeLive() {
    synchronized(stagedRedirects) {
      log.debug("Promoting {} staged redirects, replacing {} old live redirects", stagedRedirects.size(), liveRedirects.size());
      liveRedirects.clear();
      liveRedirects.addAll(stagedRedirects);
    }
  }
  
  public Redirect requestMatches(String pathInfo, String queryString) {
    Redirect matched = null;
    log.trace("Checking pathInfo {}, and queryString {}", pathInfo, queryString);
    if(liveRedirects != null && !liveRedirects.isEmpty()) {
      if(queryString != null && queryString.length() > 0) {
        for(Redirect redir : liveRedirects) {
          if(redir.matches(pathInfo+"?"+queryString)) {
            matched = (Redirect)redir.clone();
            break;
          }
        }
      }
      if(matched == null) {
        for(Redirect redir : liveRedirects) {
          if(redir.matches(pathInfo)) {
            matched = (Redirect)redir.clone();
            break;
          }
        }
      }
    }
    return matched;
  }

}
