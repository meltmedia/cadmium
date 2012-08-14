package com.meltmedia.cadmium.core.config.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.meltmedia.cadmium.core.config.PropertiesWriter;

public class PropertiesWriterImpl implements PropertiesWriter {

  @Override
  public void persistProperties(Properties properties, String fileName, String message, Logger log) {

    File propsFile = new File(fileName);
    if(propsFile.canWrite() || !propsFile.exists()) {

      if(!properties.isEmpty()) {

        ensureDirExists(propsFile.getParent());
        FileOutputStream out = null;

        try {

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

  private static void ensureDirExists(String dir) {
    File file = new File(dir);
    if(!file.exists()) {
      file.mkdirs();
    }
  }

}
