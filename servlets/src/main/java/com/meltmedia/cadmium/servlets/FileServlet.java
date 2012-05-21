package com.meltmedia.cadmium.servlets;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.meta.MimeTypeConfigProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

@SuppressWarnings("serial")
@Singleton
public class FileServlet extends net.balusc.webapp.FileServlet implements ContentService {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	protected MimeTypeConfigProcessor mimeTypes;
	
	@Inject
	@Named("config.properties")
	protected Properties configProperties;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}	

  @Override
	public void switchContent() {
		
		try {
		  if(configProperties.containsKey("com.meltmedia.cadmium.lastUpdated")) {
		    log.info("Switching to new directory ["+configProperties.getProperty("com.meltmedia.cadmium.lastUpdated")+"]");
		    this.setBasePath(configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"));
		  } else {
		    log.error("Failed to get last updated path");
		  }
			
		} catch (ServletException e) {
			log.error("Problem while setting new directory: {}", e);
		}
		
	}
  
  @Override
  protected String resolveMimeType(String filename) {
    String contentType = super.resolveMimeType(filename);
    if(mimeTypes != null) {
      String ctype = mimeTypes.getContentType(filename);
      if(ctype != null && ctype.length() > 0) {
        contentType = ctype;
      }
    }
    return contentType;
  }

}
