package com.meltmedia.cadmium.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.meltmedia.cadmium.jgroups.ContentService;
import com.meltmedia.cadmium.jgroups.ContentServiceListener;

@SuppressWarnings("serial")
@Singleton
public class FileServlet extends net.balusc.webapp.FileServlet implements ContentService {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	protected ContentServiceListener listener;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}	



  @Override
	public void switchContent(String newDir) {
		
		try {
			this.setBasePath(newDir);
			
		} catch (ServletException e) {
			log.error("Problem while setting new directory: {}", e);
		}
		
		listener.doneSwitching();
		
	}

	@Override
	public void setListener(ContentServiceListener listener) {
		
		this.listener = listener;		
	}

}
