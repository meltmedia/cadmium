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

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.FileSystemManager;

@Singleton
public class SslRedirectConfigProcessor implements ConfigProcessor {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String CONFIG_FILE_NAME = "secure.json";
  
  protected List<String> stagedSslPaths = new ArrayList<String>();
  protected List<Pattern> stagedSslPatterns = new ArrayList<Pattern>();
  
  protected List<String> liveSslPaths = new ArrayList<String>();
  protected List<Pattern> liveSslPatterns = new ArrayList<Pattern>();

  @Override
  public void processFromDirectory(String metaDir) throws Exception {
    synchronized(stagedSslPaths) {
      synchronized(stagedSslPatterns) {
        if(metaDir != null) {
          String sslFile = FileSystemManager.getFileIfCanRead(metaDir, CONFIG_FILE_NAME);
          if(sslFile != null) {
            Gson gson = new Gson();
            Collection<Map<String, String>> ssls = null;
            try {
              ssls = gson.fromJson(new FileReader(sslFile), new TypeToken<Collection<Map<String, String>>>(){}.getType());
            } catch(Exception e) {
              log.error("Invalid "+CONFIG_FILE_NAME+"!", e);
              throw e;
            }
            if(ssls != null && !ssls.isEmpty()) {
              stagedSslPaths.clear();
              stagedSslPatterns.clear();
              for(Map<String, String> ssl: ssls) {
                if(ssl.containsKey("url")) {
                  String url = ssl.get("url");
                  Pattern urlPattern = null;
                  try {
                    urlPattern = Pattern.compile(url);
                  } catch(Exception e) {
                    log.debug("Not a regex pattern", e);
                  }
                  if(urlPattern != null) {
                    stagedSslPatterns.add(urlPattern);
                  } else {
                    stagedSslPaths.add(url);
                  }
                }
              }
            } else {
              stagedSslPaths.clear();
              stagedSslPatterns.clear();
            }
          } else {
            stagedSslPaths.clear();
            stagedSslPatterns.clear();
          }
        } else {
          stagedSslPaths.clear();
          stagedSslPatterns.clear();
        }
      }
    }
  }

  @Override
  public void makeLive() {
    synchronized(stagedSslPaths) {
      synchronized(stagedSslPatterns) {
        liveSslPaths.clear();
        liveSslPatterns.clear();
        liveSslPaths.addAll(stagedSslPaths);
        liveSslPatterns.addAll(stagedSslPatterns);
      }
    }
  }
  
  public boolean shouldBeSsl(String pathInfo) {
    if(!liveSslPaths.isEmpty()) {
      for(String path : liveSslPaths) {
        if(path.equals(pathInfo)) {
          return true;
        }
      }
    }
    if(!liveSslPatterns.isEmpty()) {
      for(Pattern regex : liveSslPatterns) {
        if(regex.matcher(pathInfo).matches()) {
          return true;
        }
      }
    }
    return false;
  }

}
