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
package com.meltmedia.cadmium.core.config;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;

/**
 *  PropertiesReader Interface.  For use in the ConfigManager class. 
 * 
 * @author Brian Barr
 */

public interface PropertiesReader {

  public Properties getProperties(File file, Logger log);
  public Properties getProperties(ServletContext context, String path, Logger log);
  public Properties appendProperties(Properties properties, File configFile, Logger log);
  
}
