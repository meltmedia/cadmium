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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Singleton
public class SslRedirectConfigProcessor implements ConfigProcessor {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String CONFIG_FILE_NAME = "secure.json";

  protected Configuration stagedConfiguration = new Configuration();

  protected Configuration liveConfiguration = new Configuration();

  @Override
  public void processFromDirectory(String metaDir) throws Exception {
    Configuration newStagedConfiguration = new Configuration();
    if(metaDir != null) {
      String sslFile = FileSystemManager.getFileIfCanRead(metaDir, CONFIG_FILE_NAME);
      if(sslFile != null) {
        Gson gson = new Gson();
        Collection<Map<String, String>> ssls = null;
        try {
          ssls = gson.fromJson(FileUtils.readFileToString(new File(sslFile)), new TypeToken<Collection<Map<String, String>>>(){}.getType());
        } catch(Exception e) {
          log.error("Invalid "+CONFIG_FILE_NAME+"!", e);
          throw e;
        }
        if(ssls != null && !ssls.isEmpty()) {
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
                newStagedConfiguration.sslPatterns.add(urlPattern);
              } else {
                newStagedConfiguration.sslPaths.add(url);
              }
            }
          }
        }
      }
    }
    stagedConfiguration = newStagedConfiguration;
  }

  @Override
  public void makeLive() {
    liveConfiguration = stagedConfiguration;
  }
  
  public boolean shouldBeSsl(String pathInfo) {
    Configuration workingConfig = liveConfiguration;
    if(!workingConfig.sslPaths.isEmpty()) {
      for(String path : workingConfig.sslPaths) {
        if(path.equals(pathInfo)) {
          return true;
        }
      }
    }
    if(!workingConfig.sslPatterns.isEmpty()) {
      for(Pattern regex : workingConfig.sslPatterns) {
        if(regex.matcher(pathInfo).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  protected static class Configuration {
    List<String> sslPaths = new ArrayList<String>();
    List<Pattern> sslPatterns = new ArrayList<Pattern>();
  }

}
