package com.meltmedia.cadmium.servlets;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for regex DOS vulnerability and to test for broad acceptance of spec.
 */
public class FileServletBugTest {


  @Test
  public void parseEtagListWithQuotesTest() throws Exception {
    final String etag = "/\"/_87658765";
    final String etags = "\""+etag+"\"";

    List<String> etagsMatched = BasicFileServlet.parseETagList(etags);

    assertNotNull(etagsMatched);
    assertEquals(0, etagsMatched.size());
  }

  @Test
  public void parseEtagListWithGoodEscapesTest() throws Exception {
    final String etag = "/file name \\ with \"hello\" spaces/_87658765";
    final String etags = "\""+etag.replace("\\", "\\\\").replace("\"", "\\\"")+"\"";

    List<String> etagsMatched = BasicFileServlet.parseETagList(etags);

    assertNotNull(etagsMatched);
    assertEquals(1, etagsMatched.size());
    assertEquals(etag, etagsMatched.get(0));
  }

  @Test
  public void parseEtagListUnterminatedTest() throws Exception {
    final String etag = "/file name with hello spaces/_87658765";
    final String etags = "\""+etag+"";

    List<String> etagsMatched = BasicFileServlet.parseETagList(etags);

    assertNotNull(etagsMatched);
    assertEquals(0, etagsMatched.size());
  }

  @Test
  public void parseEtagListWithMultipleTest() throws Exception {
    final String etag1 = "/file1/_87658765";
    final String etag2 = "/file2/_87658455";
    final String etag3 = "/file3/_87658455";
    final String etags = "\""+etag1+"\",\"" + etag2 + "\",\"" + etag3 + "\"";

    List<String> etagsMatched = BasicFileServlet.parseETagList(etags);

    assertNotNull(etagsMatched);
    assertEquals(3, etagsMatched.size());
    assertEquals(etag1, etagsMatched.get(0));
    assertEquals(etag2, etagsMatched.get(1));
    assertEquals(etag3, etagsMatched.get(2));
  }
}
