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
package com.meltmedia.cadmium.core.meta;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class CommonMimeTypeTest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { "/js/some.js", "application/javascript" },
        { "/css/some.css", "text/css" },
        { "/index.html", "text/html" } });
  }
  
  private static MimeTypeConfigProcessor processor;
  
  @BeforeClass
  public static void setup() throws Exception {
    processor = new MimeTypeConfigProcessor();
    processor.processFromDirectory("src/test/resources/notFound.json"); // init without an app level config.
    processor.makeLive();
  }

  private String path;
  private String expected;
  
  public CommonMimeTypeTest( String path, String expected ) {
    this.path = path;
    this.expected = expected;
  }
  
  @Test
  public void testMapping() {
    assertEquals("Could not find content type.", expected, processor.getContentType(path));
  }
}
