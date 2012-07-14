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
