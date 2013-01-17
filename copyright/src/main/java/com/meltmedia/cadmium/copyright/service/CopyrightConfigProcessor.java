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
package com.meltmedia.cadmium.copyright.service;

import java.io.File;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.meltmedia.cadmium.core.meta.ConfigProcessor;

/**
 * <p>This will update all of the copyright dates contained in any html 
 * file with the current year. This will happen on content update.</p>
 * <p>The copyright date must be the only thing in a html element that 
 * has an attribute of <code>data-cadmium="copyright"</code>. If anything 
 * else is contained with in the html element, it will be replaced when 
 * this runs.</p> 
 * 
 * @author John McEntire
 *
 */
@Singleton
public class CopyrightConfigProcessor implements ConfigProcessor {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Inject
  protected Injector injector;

  @Override
  public void processFromDirectory(String metaDir) throws Exception {
    log.debug("Processing html files in '{}' for copyright update.", metaDir);
    ResourceHandler handler = injector.getInstance(ResourceHandler.class);
    log.trace("Got handler from injector. {}", handler);
    File contentDirectory = new File(metaDir).getAbsoluteFile().getParentFile();
    if(contentDirectory.exists()) {
      log.trace("The contentDirectory '{}' exists.", contentDirectory);
      Collection<File> htmlFiles = FileUtils.listFiles(contentDirectory, new String[] {"html", "htm"}, true);
      log.debug("Found {} html files.", htmlFiles.size());
      for(File htmlFile : htmlFiles) {
        log.trace("Processing {}", htmlFile);
        handler.handleFile(htmlFile);
      }
    }
  }

  @Override
  public void makeLive() {}

}
