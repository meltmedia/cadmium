package com.meltmedia.cadmium.core.worker;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;

public class InitializeTask implements Callable<GitService> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private String repoUri = null;
  private String branch = null;
  private String contentRoot = null;
  private String warName = null;
  private SiteConfigProcessor metaProcessor = null;
  private String contentDirectory = null;
  private ContentService servlet = null;
  private ConfigManager configManager;
  
  
  @Inject
  public InitializeTask(ConfigManager configManager, ContentService servlet, SiteConfigProcessor metaProcessor, @Named("com.meltmedia.cadmium.git.uri") String repoUri, @Named("initialCadmiumBranch") String branch, @Named("sharedContentRoot") String contentRoot, @Named("warName") String warName, @Named("contentDir") String contentDirectory) {
    this.branch = branch;
    this.repoUri = repoUri;
    this.contentRoot = contentRoot;
    this.warName = warName;
    this.contentDirectory = contentDirectory;
    this.metaProcessor = metaProcessor;
    this.servlet = servlet;
    this.configManager = configManager;
    
  }

  @Override
  public GitService call() throws Exception {
    GitService cloned = null;
    if(repoUri != null && branch != null) {
      Throwable t = null;
      
      try {
        logger.debug("Attempting to initialize content for `{}` into `{}`", warName, contentRoot);
        cloned = GitService.initializeContentDirectory(repoUri, branch, contentRoot, warName, configManager);
        if(metaProcessor != null) {
          logger.debug("Processing META-INF dir in `{}`", warName);
          this.metaProcessor.processDir(contentDirectory);
        }
        if(servlet != null) {
          logger.debug("Switching content root of {}", warName);
          this.servlet.switchContent(System.currentTimeMillis());
        }
        logger.debug("Successfully initialized `{}`", warName);
      } catch(RefNotFoundException e) {
        logger.warn("Branch `"+branch+"` does not exist.", e);
        return null;
      } catch(Throwable t1) {
        logger.error("Failed to initialize git repo `"+warName+"`", t);
        t = t1;
      }
      if(t != null) {
        throw new Exception(t);
      }
    }
    return cloned;
  }
  
  @Override
  public String toString() {
    return warName;
  }

}
