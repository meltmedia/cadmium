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
package com.meltmedia.cadmium.servlets;

import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.meta.MimeTypeConfigProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.*;
import java.util.Properties;

@SuppressWarnings("serial")
@Singleton
public class FileServlet extends BasicFileServlet implements ContentService {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	protected MimeTypeConfigProcessor mimeTypes;
	
	@Inject
	protected ConfigManager configManager;
	
	void setMimeTypeConfigProcessor( MimeTypeConfigProcessor mimeTypes ) { this.mimeTypes = mimeTypes; }	
	void setLastModifiedForTesting(long lastModified) {
	  super.setLastUpdated(lastModified);
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}	

  @Override
	public void switchContent(Long requestTime) {
    
    Properties configProperties = configManager.getDefaultProperties();

		try {
		  if(configProperties.containsKey("com.meltmedia.cadmium.lastUpdated")) {
		    log.info("Switching to new directory ["+configProperties.getProperty("com.meltmedia.cadmium.lastUpdated")+"]");
		    this.setBasePath(configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"));
		    setLastUpdated(requestTime.longValue());
		  } else {
		    log.error("Failed to get last updated path");
		    throw new RuntimeException("Failed to switch content! Configuration properties key 'com.meltmedia.cadmium.lastUpdated' not set.");
		  }
			
		} catch (ServletException e) {
			log.error("Problem while setting new directory: {}", e);
		}
		
	}
  
  @Override
  public String lookupMimeType(String filename) {
    if( mimeTypes == null) throw new RuntimeException("The mime type processor is not set!.");
    return mimeTypes.getContentType(filename);
  }
  
  @Override
	public String getContentRoot() {		
		return getBasePath();
	}
	
	/**
	 * Returns the content type for the specified path.
	 * 
	 * @param path the path to look up.
	 * @return the content type for the path.
	 * @throws FileNotFoundException if a file (or welcome file) does not exist at path.
	 * @throws IOException if any other problem prevents the lookup of the content type.
	 */
	public String contentTypeOf( String path ) throws IOException {
	  File file = findFile(path);
	  return lookupMimeType(file.getName());
	}
	
	/**
	 * Returns the file object for the given path, including welcome file lookup.  If the file cannot be found, a
	 * FileNotFoundException is returned.
	 * 
	 * @param path the path to look up.
	 * @return the file object for that path.
	 * @throws FileNotFoundException if the file could not be found.
	 * @throws IOException if any other problem prevented the locating of the file.
	 */
	public File findFile( String path ) throws IOException {
	  File base = new File(getBasePath());
	  File pathFile = new File(base, "."+path);
	  if( !pathFile.exists()) throw new FileNotFoundException("No file or directory at "+pathFile.getCanonicalPath());
	  if( pathFile.isFile()) return pathFile;
	  pathFile = new File(pathFile, "index.html");
	  if( !pathFile.exists() ) throw new FileNotFoundException("No welcome file at "+pathFile.getCanonicalPath());
	  return pathFile;
	}

  @Override
  public InputStream getResourceAsStream(String path) throws IOException {
    if( path.charAt(0) != '/' ) throw new IllegalArgumentException("The path "+path+" does not start with a '/' character.");
    File file = new File(getContentRoot(), "."+path);
    if( !file.exists() ) return null;
    return new FileInputStream(file);
  }

}
