package com.meltmedia.cadmium.servlets;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.core.FileSystemManager;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ErrorPageFilter implements Filter {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  private ContentService contentService;

  private final String DEFAULT_ERROR_PAGE = "defaultError.html";

  private ServletContext context;


  @Override
  public void init(FilterConfig config)
      throws ServletException {
    this.context = config.getServletContext();

  }

  @Override
  public void destroy() {

  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    StatusResponse wrapper = new StatusResponse((HttpServletResponse)res);

    chain.doFilter(req, wrapper);

    int code = wrapper.getStatus();  
    System.out.println("Status code: " + code);
    System.out.println("wrapper.isCommitted(): " + wrapper.isCommitted());

    if(400 <= code && code < 600 && !wrapper.isCommitted()) {

      wrapper.reset();     
      String contentPath = contentService.getContentRoot();
      System.out.println("contentPath: " + contentPath);

      //if file not found serve default error page      
      String fileName = getErrorPage(contentPath, code);      
      System.out.println("Error File name: " + fileName);

      String errorPageContent = null;
      String realPath = context.getRealPath(fileName.trim());
      System.out.println("Complete path of error page file: " + realPath);

      try {

        errorPageContent = FileSystemManager.getFileContents(realPath);       
        wrapper.getWriter().write(errorPageContent);
      }
      catch(Exception e) {

        log.error("Could not serve error page content from: {}", fileName);        
        try {

          String realPathDefault = context.getRealPath(DEFAULT_ERROR_PAGE.trim());
          errorPageContent = FileSystemManager.getFileContents(realPathDefault);           
          wrapper.getWriter().write(errorPageContent);
        }
        catch(Exception d) {

          log.error("Could not serve default error page content from: {}", DEFAULT_ERROR_PAGE);
        }
      }
      finally {

        wrapper.getWriter().close();
      }

    }    

  }

  // returns the appropriate error page based on the input error code
  public String getErrorPage(String path, int code) { 

    boolean fileFound = false;
    String fileName = "";    

    //TODO: go get output for corresponding error and serve it up if it exists
    String fileCheck = Integer.toString(code) + ".html";

    log.debug("fileCheck: {}", fileCheck);
    if(fileExists(path, fileCheck)) {

      fileFound = true;
      fileName = fileCheck;
    }
    else {  //if file not found do this the check for NNx.html 

      String secondFileCheck = fileCheck.substring(0, 2); // this should cut off the third digit f the code
      secondFileCheck += "x.html";

      log.debug("secondFileCheck: {}", secondFileCheck);
      if(fileExists(path, secondFileCheck)) {

        fileFound = true;
        fileName = secondFileCheck;
      }
      else {   //if file not found do this and check for Nxx.html

        String thirdFileCheck = secondFileCheck.substring(0, 1); // this should cut off the 2nd digit of the code
        thirdFileCheck += "xx.html";

        log.debug("thirdFileCheck: {}",  thirdFileCheck);
        if(fileExists(path, thirdFileCheck)) {  

          fileFound = true;
          fileName = thirdFileCheck;
        }
      }
    }    

    if(fileFound) {

      return fileName;
    }   

    return DEFAULT_ERROR_PAGE;      

  }  

  public boolean fileExists(String path, String fileName) {

    File file = new File(path+fileName);
    log.debug( "Does {} exist? : {}", path+fileName, file.exists());  

    return file.exists();
  }

  static class StatusResponse extends HttpServletResponseWrapper {

    private int status;

    public int getStatus() {
      return status;
    }

    public StatusResponse(HttpServletResponse response) {
      super(response);

    }

    @Override
    public void sendError(int sc) throws IOException {
      status = sc;
      super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
      status = sc;
      super.sendError(sc, msg);
    }

    @Override
    public void setStatus(int status) {     
      this.status = status;
      super.setStatus(status);
    }    

  }  

}

