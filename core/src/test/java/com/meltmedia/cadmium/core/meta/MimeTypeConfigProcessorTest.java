package com.meltmedia.cadmium.core.meta;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MimeTypeConfigProcessorTest {

  @Test
  public void getContentTypeTest() throws Exception {
    MimeTypeConfigProcessor proc = new MimeTypeConfigProcessor();
    proc.liveMimeTypes.add(new MimeType());
    proc.liveMimeTypes.get(0).setContentType("text/html");
    proc.liveMimeTypes.get(0).setExtension("html");
   
    String html = proc.getContentType("index.html");
    assertTrue("Incorrect content type returned", html != null && html.equals("text/html"));
  }
  
  @Test
  public void makeLiveTest() throws Exception {
    MimeTypeConfigProcessor proc = new MimeTypeConfigProcessor();
    proc.stagedMimeTypes.add(new MimeType());
    proc.stagedMimeTypes.get(0).setContentType("text/html");
    proc.stagedMimeTypes.get(0).setExtension("html");
    proc.makeLive();
    
    assertTrue("Not Promoted", proc.liveMimeTypes.size() == 1);
    assertTrue("Not Promoted correctly", proc.liveMimeTypes.get(0) == proc.stagedMimeTypes.get(0));
  }
  
  @Test
  public void processFromDirectoryTest() throws Exception {
    MimeTypeConfigProcessor proc = new MimeTypeConfigProcessor();
    proc.processFromDirectory("./src/test/resources");
    
    assertTrue("File not parsed", proc.stagedMimeTypes.size() == 3);
    for(MimeType mime : proc.stagedMimeTypes) {
      assertTrue("Mime Type not processed", mime.getExtension() != null && mime.getExtension().length() > 0 && mime.getContentType() != null && mime.getContentType().length() > 0);
    }
  }
}
