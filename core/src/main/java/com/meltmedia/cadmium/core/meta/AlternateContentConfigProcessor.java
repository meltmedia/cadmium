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
import java.util.regex.Pattern;

/**
 * Parses the <em>alternate-content.json</em> file.
 *
 * @author John McEntire
 */
@Singleton
public class AlternateContentConfigProcessor implements ConfigProcessor {
  public static final String CONFIG_FILE_NAME = "alternate-content.json";
  private final Logger logger = LoggerFactory.getLogger(getClass());
  protected Configuration stagedConfig;
  protected Configuration liveConfig;

  @Override
  public void processFromDirectory(String metaDir) throws Exception {
    Configuration newStagedConfig = new Configuration();
    if (metaDir != null) {
      String configFile = FileSystemManager.getFileIfCanRead(metaDir, CONFIG_FILE_NAME);
      if (configFile != null) {
        newStagedConfig.metaDir = new File(metaDir);
        String contents = FileUtils.readFileToString(new File(configFile));
        logger.debug("Parsing {}/{}", new Object[]{newStagedConfig.metaDir, CONFIG_FILE_NAME});
        Collection<AlternateContent> parsedConfigs = new Gson().fromJson(contents, new TypeToken<Collection<AlternateContent>>(){}.getType());
        for(AlternateContent config: parsedConfigs) {
          config.compiledPattern = Pattern.compile(config.getPattern());
          newStagedConfig.configs.add(config);
        }
      }
    }
    stagedConfig = newStagedConfig;
  }

  @Override
  public void makeLive() {
    logger.trace("Promoting {} staged alternate content configurations.", stagedConfig.configs.size());
    liveConfig = stagedConfig;
  }

  /**
   * Iterates through AlternateContent objects trying to match against their pre compiled pattern.
   *
   * @param userAgent The userAgent request header.
   * @return the ContentDirectory of the matched AlternateContent instance or null if none are found.
   */
  public File getAlternateContentDirectory(String userAgent) {
    Configuration config = liveConfig;
    for(AlternateContent configuration: config.configs) {
      try {
        if(configuration.compiledPattern.matcher(userAgent).matches()) {
          return new File(config.metaDir, configuration.getContentDirectory());
        }
      } catch(Exception e) {
        logger.warn("Failed to process config: "+ configuration.getPattern()+"->"+ configuration.getContentDirectory(), e);
      }
    }
    return null;
  }

  public boolean hasConfig() {
    return liveConfig != null && liveConfig.configs.size() > 0;
  }

  private class Configuration {
    List<AlternateContent> configs = new ArrayList<AlternateContent>();
    File metaDir;
  }
}
