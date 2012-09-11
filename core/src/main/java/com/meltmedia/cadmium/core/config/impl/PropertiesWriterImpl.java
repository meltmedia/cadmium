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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.meltmedia.cadmium.core.config.PropertiesWriter;

/**
 * This implements the PropertiesWriter Interface.  For use in the ConfigManager class 
 * 
 * @author Brian Barr
 */

public class PropertiesWriterImpl implements PropertiesWriter {

  @Override
  public void persistProperties(Properties properties, File propsFile, String message, Logger log) {
    
    if(propsFile.canWrite() || !propsFile.exists()) {

      if(!properties.isEmpty()) {

        
        FileOutputStream out = null;

        try {

          ensureDirExists(propsFile.getParent());
          out = new FileOutputStream(propsFile);
          properties.store(out, message);
          out.flush();
        } 
        catch(Exception e) {

          log.warn("Failed to persist vault properties file.", e);
        } 
        finally {
          IOUtils.closeQuietly(out);
        }
      }
    }

  }
  
  
  private static void ensureDirExists(String dir) throws IOException {
    File file = new File(dir);
    FileUtils.forceMkdir(file);
  }

}
