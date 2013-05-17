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
package com.meltmedia.cadmium.copyright.service;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    String postUpdateFile = FileUtils.readFileToString(postUpdate);
    assertEquals("File didn't update as suspected.", FileUtils.readFileToString(preUpdate), postUpdateFile);
  }

}
