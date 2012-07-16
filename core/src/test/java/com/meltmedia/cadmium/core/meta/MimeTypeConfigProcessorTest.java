/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.meta;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class MimeTypeConfigProcessorTest {

  @Test
  public void getContentTypeTest() throws Exception {
    MimeTypeConfigProcessor proc = new MimeTypeConfigProcessor();
    proc.mimeTypes.put("html", "text/html");
   
    String html = proc.getContentType("index.html");
    assertTrue("Incorrect content type returned", html != null && html.equals("text/html"));
  }
  
  @Test
  public void makeLiveTest() throws Exception {
    MimeTypeConfigProcessor proc = new MimeTypeConfigProcessor();
    proc.stagedMimeTypes.put("html", "text/html");
    proc.makeLive();
    
    assertTrue("Not Promoted", proc.mimeTypes.size() == 1);
  }
  
  @Test
  public void processFromDirectoryTest() throws Exception {
    MimeTypeConfigProcessor proc = new MimeTypeConfigProcessor();
    proc.processFromDirectory("./src/test/resources");
    assertTrue("There should be more content types, due to the default file.", proc.stagedMimeTypes.size() > 850);
    for( Map.Entry<String, String> entry : proc.stagedMimeTypes.entrySet() ) {
      assertTrue("A zero length extension was found.", entry.getKey().length() > 0);
      assertTrue("A zero length content type was found.", entry.getValue().length() > 0);
    }
  }
}
