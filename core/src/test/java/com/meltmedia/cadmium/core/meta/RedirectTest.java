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
