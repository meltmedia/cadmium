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
package com.meltmedia.cadmium.core.worker;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.core.history.HistoryManager;

public class ConfigInitializeTask implements Callable<GitService> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private String contentRoot = null;
  private String warName = null;
  private ConfigManager configManager;  
  private HistoryManager historyManager = null;

  @Inject
  public ConfigInitializeTask(ConfigManager configManager, @Named("sharedContentRoot") String contentRoot, @Named("warName") String warName, HistoryManager historyManager) {
    
    this.contentRoot = contentRoot;
    this.warName = warName;
    this.configManager = configManager;
    this.historyManager = historyManager;
  }

  @Override
  public GitService call() throws Exception {
    
    Properties configProperties = configManager.getDefaultProperties();
    String branch = configProperties.getProperty("com.meltmedia.cadmium.config.branch");
    String repoUri = configProperties.getProperty("com.meltmedia.cadmium.config.git.uri", configProperties.getProperty("com.meltmedia.cadmium.git.uri"));
    
    GitService cloned = null;
    if(repoUri != null && branch != null) {
      Throwable t = null;

      try {
        logger.info("Attempting to initialize config for `{}` into `{}`", warName, contentRoot);
        cloned = GitService.initializeConfigDirectory(repoUri, branch, contentRoot, warName, historyManager, configManager);

        String contentDirectory = configProperties.getProperty("com.meltmedia.cadmium.config.lastUpdated");
        
        if(configManager != null && contentDirectory != null) {
          logger.info("Processing configuration dir in `{}`", warName);
          this.configManager.parseConfigurationDirectory(new File(contentDirectory));
        }
        configManager.makeConfigParserLive();
        logger.info("Successfully initialized config for `{}`", warName);
      } catch(RefNotFoundException e) {
        logger.warn("Branch `"+branch+"` does not exist.", e);
        return null;
      } catch(Throwable t1) {
        logger.error("Failed to initialize config git repo `"+warName+"`", t1);
        t = t1;
      }
      if(t != null) {
        throw new Exception(t);
      }
    } else {
      logger.warn("Invalid setting for configuration repo {} and branch {}", repoUri, branch);
    }
    return cloned;
  }

  @Override
  public String toString() {
    return warName;
  }

}
