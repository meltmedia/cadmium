package com.meltmedia.cadmium.email.jersey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.email.model.EmailForm;
import com.meltmedia.cadmium.mail.EmailException;
import com.meltmedia.cadmium.mail.VelocityHtmlTextEmail;
import com.meltmedia.cadmium.mail.internal.EmailServiceImpl;

@Path("/api/email")
public class EmailService {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	private EmailServiceImpl emailService;
	
	@Inject
	private ContentService contentService;
	
	@POST
  @Consumes("application/x-www-form-urlencoded")
  @Produces(MediaType.APPLICATION_JSON)
	public Response emailThisPage(@Context HttpServletRequest request,@FormParam("dir") String dir,@FormParam("toName") String toName,
			                          @FormParam("toAddress") String toAddress,@FormParam("fromName") String fromName,
			                          @FormParam("fromAddress") String fromAddress,@FormParam("message") String message,
			                          @FormParam("pagePath") String pagePath) {

  	log.info("Entering Email This Method");
  	VelocityHtmlTextEmail email = new VelocityHtmlTextEmail();
  	
  	// Setting up template location/files
  	File absoluteTemplateDir = new File(contentService.getContentRoot(),"META-INF");
  	absoluteTemplateDir = new File(absoluteTemplateDir,dir);
	  File textTemplateFile = new File(absoluteTemplateDir,"email-this-page.txt");
	  log.info("textTemplateFile: {}", textTemplateFile.getPath());
  	File htmlTemplateFile = new File(absoluteTemplateDir,"email-this-page.html");
  	log.info("htmlTemplateFile: {}", htmlTemplateFile.getPath());
  	if (textTemplateFile.exists() && htmlTemplateFile.exists()) {
  		if (pageExists(pagePath)) {
		  	try { 
		  		EmailForm emailForm = new EmailForm(toName, toAddress, fromName, fromAddress, message, pagePath);
					EmailFormValidator.validate(emailForm);
			  	
			  	email.addTo(emailForm.getToAddress());
			  	email.setFrom(emailForm.getFromAddress()); 
			  	// Set HTML Template
			  	email.setHtml(readFromFile(htmlTemplateFile.getAbsolutePath()));
			  	
			  	// Set Text Template
			  	email.setText(readFromFile(textTemplateFile.getAbsolutePath()));
			  	
			  	// Set Properties
			  	email.setProperty("toName", emailForm.getToName());
			  	email.setProperty("fromName", emailForm.getFromName());
			  	email.setProperty("fromAddress", emailForm.getFromAddress());
			  	email.setProperty("message", emailForm.getMessage());
			  	
			  	// Set up link
			  	String link = "http://" + request.getServerName() + "/"  + emailForm.getPagePath();
			  	log.info("Email This Page Link: {}",link);
			  	email.setProperty("link",link);
								  	
			  	// Send Email
			  	log.debug("Before Sending Email");  		
			  	emailService.send(email);
			  	log.debug("After Sending Email");
				} catch (EmailException e) {
					log.info("EmailException Caught");
					return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
				} catch (ValidationException e) {
					log.info("ValidationException Caught");
					log.info("First Error {}",e.getErrors()[0].getMessage());
					return Response.status(Response.Status.BAD_REQUEST).entity(e.getErrors()).build();
				} 	
				return Response.ok().build();
  		} else {
    		log.info("Invalid Page");
    		return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Page").build();	
  		}
  	} else {
  		log.info("Couldn't Find Email Templates");
  		return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Template Location").build();		  		
  	}
	}
	
	private boolean pageExists(String pagePath) {
		if (pagePath.contains("-INF")) {
			return false;
		}
		File pagePathFile = new File(contentService.getContentRoot(),pagePath);
		return pagePathFile.exists();
	}

	/*
   * Reads data from a given file
   */
  public String readFromFile(String fileName) {
    String content = null;
    BufferedReader br = null;
    try {
      File inFile = new File(fileName);
      br = new BufferedReader(new InputStreamReader(
          new FileInputStream(inFile)));
      content = "";
      while(br.ready()) {
      	content += br.readLine();
      }
      br.close();
    } catch (FileNotFoundException ex) {
    	log.error("Failed to find file.", ex);
    } catch (IOException ex) {
    	log.error("Failed to read file.", ex);
    } finally {
    	if(br != null) {
    		try {
    			br.close();
    		} catch(Exception e){}
    	}
    }
    return content;

  }
  
}
