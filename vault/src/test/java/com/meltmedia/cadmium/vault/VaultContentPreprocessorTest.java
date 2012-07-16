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
package com.meltmedia.cadmium.vault;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.vault.service.DummyVaultService;

public class VaultContentPreprocessorTest {
  public static final String ORIG_SAFETY = "  \n\n  \t <div id=\"safety\">\nThis is the original safety.</div>";
  public static final String ORIG_HTML = "<html>\n <body>\n\n  <div data-vault-guid=\"guid\">"+ORIG_SAFETY+"</div>\n\n \t</body>\n</html>";
  public static final String NEW_SAFETY = "  \n\n  \t <div id=\"safety\">\nThis is the new safety.</div>";
  public static final String NEW_HTML = "<html>\n <body>\n\n  <div data-vault-guid=\"guid\">"+NEW_SAFETY+"</div>\n\n \t</body>\n</html>";
  
  @Before
  public void createHtmlFile() throws Exception {
    new File("target/test-preprocessor/META-INF").mkdirs();
    new File("target/test-preprocessor/").mkdirs();
    FileSystemManager.writeStringToFile("target/test-preprocessor", "index.html", ORIG_HTML);

    new File("target/test-preprocessor/dir").mkdirs();
    FileSystemManager.writeStringToFile("target/test-preprocessor/dir", "index.html", ORIG_HTML);
  }

  @Test
  public void testProcessHtmlFile() throws Exception {
    System.out.println("testProcessHtmlFile");
    VaultContentPreprocessor proc = new VaultContentPreprocessor();
    DummyVaultService service = new DummyVaultService();
    service.setResource(NEW_SAFETY);
    proc.loader = service;
    
    proc.processHtmlFile("target/test-preprocessor/index.html");
    String newContent = FileSystemManager.getFileContents("target/test-preprocessor/index.html");
    assertTrue("Content not updated", newContent != null && newContent.equals(NEW_HTML));
  }
  
  @Test
  public void testProcessFromDirectory() throws Exception {
    System.out.println("testProcessFromDirectory");
    VaultContentPreprocessor proc = new VaultContentPreprocessor();
    DummyVaultService service = new DummyVaultService();
    service.setResource(NEW_SAFETY);
    proc.loader = service;
    
    proc.processFromDirectory("target/test-preprocessor/META-INF");
    

    String newContent = FileSystemManager.getFileContents("target/test-preprocessor/index.html");
    assertTrue("Content not updated", newContent != null && newContent.equals(NEW_HTML));
    

    newContent = FileSystemManager.getFileContents("target/test-preprocessor/dir/index.html");
    assertTrue("Content dir not updated", newContent != null && newContent.equals(NEW_HTML));
  }
}
