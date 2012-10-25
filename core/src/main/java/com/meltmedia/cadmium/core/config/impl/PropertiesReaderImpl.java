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
package com.meltmedia.cadmium.core.config.impl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.meltmedia.cadmium.core.config.PropertiesReader;

/**
 * This implements the PropertiesReader Interface.  For use in the ConfigManager class 
 * 
 * @author Brian Barr
 */

public class PropertiesReaderImpl implements PropertiesReader {
  
  @Override
  public Properties getProperties(File file, Logger log) {

    Properties properties = new Properties();    
    readPropertiesWithReader(properties, file, log);
    return properties;
  }

  @Override
  public Properties getProperties(ServletContext context, String path, Logger log) {

    Properties properties = new Properties();    
    readPropertiesWithContext(properties, context, path, log);
    return properties;
  }
  
  @Override
  public Properties getProperties(InputStream stream, Logger log) {

    Properties properties = new Properties();    
    readPropertiesWithInputStream(properties, stream, log);
    return properties;
  }
 

  @Override
  public Properties appendProperties(Properties properties, File configFile, Logger log) {

    readPropertiesWithReader(properties, configFile, log);
    return properties;
  }

  private void readPropertiesWithReader(Properties properties, File file, Logger log) {

    Reader reader = null;
    try{

      log.trace("configFile path: {}", file.getPath());
      reader = new FileReader(file);
      properties.load(reader); 
    }
    catch(Exception e) {

      log.warn("Failed to load "+file.getAbsolutePath());
    }
    finally {

      IOUtils.closeQuietly(reader);
    }

  }
  
  private void readPropertiesWithInputStream(Properties properties, InputStream stream, Logger log) {
        
    try{      
      
      properties.load(stream);
    } 
    catch(Exception e) {
      
      log.warn("Failed to load properties");
    } 
    finally {
      
      IOUtils.closeQuietly(stream);
    }   

  }

  private void readPropertiesWithContext(Properties properties, ServletContext context, String path, Logger log) {

    Reader reader = null;
    try{

      reader = new InputStreamReader(context.getResourceAsStream(path), "UTF-8");
      properties.load(reader);

    } 
    catch(Exception e) {

      log.warn("Failed to load "+path);
    }
    finally {

      IOUtils.closeQuietly(reader);
    }

  }

}
