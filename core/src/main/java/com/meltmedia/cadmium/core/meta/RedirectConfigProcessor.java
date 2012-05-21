package com.meltmedia.cadmium.core.meta;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.FileSystemManager;

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
      log.info("Promoting {} staged redirects, replacing {} old live redirects", stagedRedirects.size(), liveRedirects.size());
      liveRedirects.clear();
      liveRedirects.addAll(stagedRedirects);
    }
  }
  
  public Redirect requestMatches(String pathInfo, String queryString) {
    Redirect matched = null;
    log.debug("Checking pathInfo {}, and queryString {}", pathInfo, queryString);
    if(liveRedirects != null && !liveRedirects.isEmpty()) {
      if(queryString != null && queryString.length() > 0) {
        for(Redirect redir : liveRedirects) {
          synchronized(redir) {
            if(redir.matches(pathInfo+"?"+queryString)) {
              matched = (Redirect)redir.clone();
              break;
            }
          }
        }
      }
      if(matched == null) {
        for(Redirect redir : liveRedirects) {
          synchronized(redir) {
            if(redir.matches(pathInfo)) {
              matched = (Redirect)redir.clone();
              break;
            }
          }
        }
      }
    }
    return matched;
  }

}
