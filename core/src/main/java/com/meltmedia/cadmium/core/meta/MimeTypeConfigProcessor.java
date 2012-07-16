package com.meltmedia.cadmium.core.meta;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.FileSystemManager;

@Singleton
public class MimeTypeConfigProcessor implements ConfigProcessor {
  private static final Logger log = LoggerFactory.getLogger(MimeTypeConfigProcessor.class);
  public static final String CONFIG_FILE_NAME = "mime.json";
  
  protected Map<String, String> stagedMimeTypes = new HashMap<String, String>();
  protected Map<String, String> mimeTypes = new HashMap<String, String>();
  
  @Override
  public void processFromDirectory(String metaDir) throws Exception {
    Map<String, String> newMimeTypes = new HashMap<String, String>();
      addDefaultMimeTypes(newMimeTypes);
      String mimeTypeFile = FileSystemManager.getFileIfCanRead(metaDir, CONFIG_FILE_NAME);
      if( mimeTypeFile != null ) {
        addAppMimeTypes(newMimeTypes, mimeTypeFile);
      }
    this.stagedMimeTypes = newMimeTypes;
  }
  
  static void addDefaultMimeTypes( Map<String, String> mimeTypeMap ) throws IllegalArgumentException, UnsupportedEncodingException {
    InputStream in = null;
    try {
      in = MimeTypeConfigProcessor.class.getResourceAsStream("mime.types");
      if( in == null ) {
        log.warn("The default mime type file is missing.");
        return;
      }
      LineIterator lineIterator = new LineIterator(new InputStreamReader(in, "UTF-8"));
      while( lineIterator.hasNext() ) {
        String line = lineIterator.next();
        line = line.replaceAll("(\\A[^#]*)(#.*)?\\Z", "$1").trim(); // kill comments.
        if( line.length() == 0 ) continue; // skip blank lines.
        String[] parts = line.split("\\s+");
        if( parts.length < 2 ) continue; // skip lines with no extensions.
        String type = parts[0];
        for( int i = 1; i < parts.length; i++ ) {
          mimeTypeMap.put(parts[i], type);
        }
      }
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }
  
  static void addAppMimeTypes( Map<String, String> mimeTypeMap, String mimeTypeFile ) throws Exception {
    Gson gson = new Gson();
    Collection<MimeType> mimes = null;
    try {
      mimes = gson.fromJson(new FileReader(mimeTypeFile), new TypeToken<Collection<MimeType>>(){}.getType());
      for( MimeType mime : mimes ) {
        mimeTypeMap.put(mime.getExtension(), mime.getContentType());
      }
      } catch(Exception e) {
      log.error("Invalid "+CONFIG_FILE_NAME+"!", e);
      throw e;
    }
  }

  @Override
  public void makeLive() {
    log.info("Promoting {} staged mime types, replacing {} old live mime types", stagedMimeTypes.size(), mimeTypes.size());
    this.mimeTypes = this.stagedMimeTypes;
  }
  
  public String getContentType(String filename) {
    String[] parts = filename.split("\\.");
    String mimeType = mimeTypes.get(parts[parts.length-1]);
    log.debug("Resolving mimetype for {} with extension {} to {}", new String[] {filename, parts[parts.length-1], mimeType});
    return mimeType;
  }

}
