package com.meltmedia.cadmium.servlets;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.jgroups.util.Util.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: jmcentire
 * Date: 5/16/13
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicFileServletTest {
  private static BasicFileServlet servlet;

  @BeforeClass
  public static void runBefore() {
    servlet = new BasicFileServlet();
  }

  @Test
  public void testCanAccept() {
    assertTrue(BasicFileServlet.canAccept("", false, "identity"));
    assertTrue(BasicFileServlet.canAccept("gzip", false, "identity"));
    assertTrue(!BasicFileServlet.canAccept("identity;q=0", false, "identity"));
    assertTrue(!BasicFileServlet.canAccept("indentity", true, "gzip"));
    assertTrue(BasicFileServlet.canAccept("gzip", true, "gzip"));
    assertTrue(!BasicFileServlet.canAccept("gzip;q=0", true, "gzip"));
  }
}
