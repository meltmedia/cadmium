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

public class RedirectConfigProcessorTest {

  @Test
  public void requestMatchesTest() throws Exception {
    RedirectConfigProcessor proc = new RedirectConfigProcessor();
    proc.liveRedirects.add(new Redirect());
    proc.liveRedirects.get(0).setPath("path1");
    proc.liveRedirects.get(0).setUrl("url");
    proc.liveRedirects.add(new Redirect());
    proc.liveRedirects.get(1).setPath("path2\\?info");
    proc.liveRedirects.get(1).setUrl("url2");
    
    Redirect redir = proc.requestMatches("path1", null);
    assertTrue("Path1 should have matched", redir != null && redir.getUrlSubstituted().equals("url"));
    redir = proc.requestMatches("path1", "info2");
    assertTrue("Path1 with queryString should have matched", redir != null && redir.getUrl().equals("url"));
    redir = proc.requestMatches("path2", "info");
    assertTrue("Path2 with query should have matched", redir != null && redir.getUrlSubstituted().equals("url2"));
    assertTrue("Path2 should not have matched", proc.requestMatches("path2", null) == null);
    assertTrue("Path3 should not have matched", proc.requestMatches("path3", null) == null);
  }
  
  @Test
  public void makeLiveTest() throws Exception {
    RedirectConfigProcessor proc = new RedirectConfigProcessor();
    proc.stagedRedirects.add(new Redirect());
    proc.stagedRedirects.get(0).setPath("path");
    proc.stagedRedirects.get(0).setUrl("url");
    
    proc.makeLive();
    
    assertTrue("Redirects not promoted", proc.liveRedirects.size() == 1);
    assertTrue("Redirects not promoted correctly", proc.liveRedirects.get(0).getPath().equals("path") && proc.liveRedirects.get(0).getUrl().equals("url"));
  }
  
  @Test
  public void processFromDirectoryTest() throws Exception {
    RedirectConfigProcessor proc = new RedirectConfigProcessor();
    proc.processFromDirectory("./src/test/resources");
    
    assertTrue("Redirects not parsed.", proc.stagedRedirects.size() == 3);
    for(Redirect redir : proc.stagedRedirects) {
      assertTrue("Redirect not parsed", redir.getUrl() != null && redir.getUrl().length() > 0 && redir.getPath() != null && redir.getPath().length() > 0);
    }
  }
}
