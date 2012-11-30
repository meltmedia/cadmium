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
package com.meltmedia.cadmium.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.LoggerConfig;

/**
 * Utility class for Logback management.
 * 
 * @author John McEntire
 *
 */
public class LogUtils {
  private static final Logger log = LoggerFactory.getLogger(LogUtils.class);

  public static final String LOG_DIR_INIT_PARAM = "log-directory";
  public static final String JBOSS_LOG_DIR = "jboss.server.log.dir";
  
  /**
   * @return An array of LoggerConfig Objects that represent all configured loggers.
   */
  public static LoggerConfig[] getConfiguredLoggers() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    List<ch.qos.logback.classic.Logger> loggerList = context.getLoggerList();
    List<LoggerConfig> loggers = new ArrayList<LoggerConfig>();
    for(ch.qos.logback.classic.Logger logger : loggerList) {
      if(logger.getLevel() != null) {
        loggers.add(new LoggerConfig(logger.getName(), logger.getLevel().levelStr));
        log.debug("Found configured logger: {}[{}]", logger.getName(), logger.getLevel());
      }
    }
    
    return loggers.toArray(new LoggerConfig [] {});
  }
  
  /**
   * Updates a logger with a given name to the given level.
   * 
   * @param loggerName
   * @param level
   */
  public static LoggerConfig[] setLogLevel(String loggerName, String level) {
    if(StringUtils.isBlank(loggerName)) {
      loggerName = ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME;
    }
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    log.debug("Setting {} to level {}", loggerName, level);
    ch.qos.logback.classic.Logger logger = null;
    try {
      logger = context.getLogger(loggerName);
      if(logger != null) {
        if(level.equals("null") || level.equals("none")) {
          logger.setLevel(null);
        } else {
          logger.setLevel(Level.toLevel(level));
        }
        logger = context.getLogger(loggerName);
        return new LoggerConfig[] {new LoggerConfig(logger.getName(), logger.getLevel() + "")};
      }
      return new LoggerConfig[] {};
    } catch(Throwable t) {
      log.warn("Failed to change log level for logger "+loggerName+" to level "+level, t);
      return new LoggerConfig[] {};
    }
  }

  /**
   * <p>Reconfigures the logging context.</p>
   * <p>The LoggerContext gets configured with "/WEB-INF/context-logback.xml". There is a <code>logDir</code> property set here which is expected to be the directory that the log file is written to.</p>
   * <p>The <code>logDir</code> property gets set on the LoggerContext with the following logic.</p>
   * <ul>
   *   <li>{@link LOG_DIR_INIT_PARAM} context parameter will be created and checked if it is writable.</li>
   *   <li>The File object passed in is used as a fall-back if it can be created and written to.</li>
   * </ul>    
   * @see {@link LoggerContext}
   * @param servletContext The current servlet context.
   * @param logDirFallback The fall-back directory to log to.
   * @param vHostName
   * @param log
   * @throws FileNotFoundException Thrown if no logDir can be written to.
   * @throws MalformedURLException 
   * @throws IOException
   */
  public static void configureLogback( ServletContext servletContext, File logDirFallback, String vHostName, Logger log ) throws FileNotFoundException, MalformedURLException, IOException {
    log.debug("Reconfiguring Logback!");
    String systemLogDir = System.getProperty(JBOSS_LOG_DIR);
    if (systemLogDir != null) {
      systemLogDir += "/" + vHostName;
    }
    File logDir = FileSystemManager.getWritableDirectoryWithFailovers(systemLogDir,servletContext.getInitParameter(LOG_DIR_INIT_PARAM), logDirFallback.getAbsolutePath());
    if(logDir != null) {
      log.debug("Resetting logback context.");
      URL configFile = servletContext.getResource("/WEB-INF/context-logback.xml");
      log.debug("Configuring logback with file, {}", configFile);
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      try {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.stop();
        context.reset();  
        context.putProperty("logDir", logDir.getCanonicalPath());
        configurator.doConfigure(configFile);
      } catch (JoranException je) {
        // StatusPrinter will handle this
      } finally {
        context.start();
        log.debug("Done resetting logback.");
      }
      StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }
  }

}
