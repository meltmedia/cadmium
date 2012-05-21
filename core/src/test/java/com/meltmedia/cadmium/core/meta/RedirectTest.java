package com.meltmedia.cadmium.core.meta;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RedirectTest {
  @Test
  public void matchesTest() throws Exception {
    Redirect redir = new Redirect();
    redir.setPath("^.*/(test1|test2)/.*$");
    redir.setUrl("/this/is/$1/cool.html");
    
    assertTrue("Path should have matched", redir.matches("/path/to/test2/should/match.html"));
    String pathSub = redir.getUrlSubstituted();
    assertTrue("Path returned is incorrect", pathSub != null && pathSub.equals("/this/is/test2/cool.html"));
    assertTrue("Path shouldn't have matched", !redir.matches("/this/is/a/bad/blah/path.html"));
    
    redir = new Redirect();
    redir.setPath("/path/to/something.html");
    redir.setUrl("/path/to/other.html");
    assertTrue("Path should match", redir.matches("/path/to/something.html"));
    pathSub = redir.getUrlSubstituted();
    assertTrue("Other path not returned", pathSub != null && pathSub.equals("/path/to/other.html"));
    assertTrue("Path shouldn't have matched 2", !redir.matches("/not/path/to/something.html"));
  }
}
