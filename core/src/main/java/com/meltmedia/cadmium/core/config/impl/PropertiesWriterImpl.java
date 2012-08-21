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
