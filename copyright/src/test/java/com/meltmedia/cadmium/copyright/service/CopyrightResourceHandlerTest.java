package com.meltmedia.cadmium.copyright.service;

import static org.junit.Assert.*;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Tests that the {@link CopyrightResourceHandler} class updates an html file as expected.
 * 
 * @author John McEntire
 *
 */
public class CopyrightResourceHandlerTest {
  
  @Test
  public void handleFile() throws Exception {
    File preUpdate = new File("target/test-classes/pre-update.html");
    File postUpdate = new File("target/test-classes/post-update.html");
    
    CopyrightResourceHandler handler = new CopyrightResourceHandler();
    handler.year = 2013;
    
    long preUpdateSize = preUpdate.length();
    handler.handleFile(preUpdate);
    
    assertTrue("File size didn't change.", preUpdateSize != preUpdate.length());
    assertEquals("File did't update as suspected.", FileUtils.readFileToString(preUpdate), FileUtils.readFileToString(postUpdate));
  }

}
