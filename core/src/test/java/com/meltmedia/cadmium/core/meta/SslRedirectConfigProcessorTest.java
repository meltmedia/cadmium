package com.meltmedia.cadmium.core.meta;

import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class SslRedirectConfigProcessorTest {

  @Test
  public void shouldBeSslTest() throws Exception {
    SslRedirectConfigProcessor proc = new SslRedirectConfigProcessor();
    proc.liveSslPaths.add("path1");
    proc.liveSslPatterns.add(Pattern.compile("path2"));
    
    assertTrue("Should have matched", proc.shouldBeSsl("path1") && proc.shouldBeSsl("path2"));
    assertTrue("Should not have matched", !proc.shouldBeSsl("path2/not") && !proc.shouldBeSsl("path"));
  }
  
  @Test
  public void makeLiveTest() throws Exception {
    SslRedirectConfigProcessor proc = new SslRedirectConfigProcessor();
    proc.stagedSslPaths.add("path1");
    proc.stagedSslPatterns.add(Pattern.compile("path2"));
    proc.makeLive();
    
    assertTrue("Paths not promoted", proc.liveSslPaths.size() == 1);
    assertTrue("Patterns not promoted", proc.liveSslPatterns.size() == 1);

    assertTrue("Paths not promoted correctly", proc.liveSslPaths.get(0).equals("path1"));
    assertTrue("Patterns not promoted correctly", proc.liveSslPatterns.get(0).pattern().equals("path2"));
    
    proc.stagedSslPaths.clear();
    proc.makeLive();

    assertTrue("Paths not promoted 2", proc.liveSslPaths.size() == 0);
    assertTrue("Patterns not promoted 2", proc.liveSslPatterns.size() == 1);
    assertTrue("Patterns not promoted correctly 2", proc.liveSslPatterns.get(0).pattern().equals("path2"));
  }
  
  @Test
  public void processFromDirectoryTest() throws Exception {
    SslRedirectConfigProcessor proc = new SslRedirectConfigProcessor();
    proc.processFromDirectory("./src/test/resources");
    
    assertTrue("No paths were read in.", !proc.stagedSslPaths.isEmpty() || !proc.stagedSslPatterns.isEmpty());
    for(String path : proc.stagedSslPaths) {
      System.out.println("Path ["+path+"]");
      assertTrue("Path is empty", path.length() > 0);
    }
    for(Pattern pattern : proc.stagedSslPatterns) {
      System.out.println("Pattern ["+pattern.pattern()+"]");
      assertTrue("Pattern is empty", pattern.pattern().length() > 0);
    }
  }
}
