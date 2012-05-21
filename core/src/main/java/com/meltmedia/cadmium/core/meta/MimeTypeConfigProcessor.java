package com.meltmedia.cadmium.core.meta;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.FileSystemManager;

@Singleton
public class MimeTypeConfigProcessor implements ConfigProcessor {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String CONFIG_FILE_NAME = "mime.json";
  
  protected List<MimeType> stagedMimeTypes = new ArrayList<MimeType>();
  protected List<MimeType> liveMimeTypes = new ArrayList<MimeType>();
  
  @Override
  public void processFromDirectory(String metaDir) throws Exception {
    synchronized(stagedMimeTypes) {
      if(metaDir != null) {
        String mimeTypeFile = FileSystemManager.getFileIfCanRead(metaDir, CONFIG_FILE_NAME);
        if(mimeTypeFile != null) {
          Gson gson = new Gson();
          Collection<MimeType> mimes = null;
          try {
            mimes = gson.fromJson(new FileReader(mimeTypeFile), new TypeToken<Collection<MimeType>>(){}.getType());
          } catch(Exception e) {
            log.error("Invalid "+CONFIG_FILE_NAME+"!", e);
            throw e;
          }
          if(mimes != null && !mimes.isEmpty()) {
            stagedMimeTypes.clear();
            stagedMimeTypes.addAll(mimes);
          } else {
            stagedMimeTypes.clear();
          }
        } else {
          stagedMimeTypes.clear();
        }
      } else {
        stagedMimeTypes.clear();
      }
    }
  }

  @Override
  public void makeLive() {
    synchronized(stagedMimeTypes) {
      log.info("Promoting {} staged mime types, replacing {} old live mime types", stagedMimeTypes.size(), liveMimeTypes.size());
      liveMimeTypes.clear();
      liveMimeTypes.addAll(stagedMimeTypes);
    }
  }
  
  public String getContentType(String filename) {
    String contentType = null;
    if(liveMimeTypes != null && !liveMimeTypes.isEmpty()) {
      for(MimeType mime : liveMimeTypes) {
        String ext = mime.getExtension();
        if(!ext.startsWith(".")) {
          ext = "." + ext;
        }
        if(filename.endsWith(ext)) {
          contentType = mime.getContentType();
          break;
        }
      }
    }
    return contentType;
  }

}
