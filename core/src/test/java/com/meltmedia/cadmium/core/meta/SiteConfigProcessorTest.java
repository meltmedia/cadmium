package com.meltmedia.cadmium.core.meta;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SiteConfigProcessorTest {
  
  private Set<ConfigProcessor> processors;
  private SiteConfigProcessor proc;
  
  @Before
  public void createProcessors() throws Exception {
    processors = new HashSet<ConfigProcessor>();
    for(int i=0; i<5; i++) {
      DummyConfigProcessor proc = new DummyConfigProcessor();
      processors.add(proc);
    }
    proc = new SiteConfigProcessor(processors, null);
    File testDir = new File("./target/meta-test");
    if(!testDir.exists()) {
      testDir.mkdirs();
    }
    File metaDir = new File(testDir, "META-INF");
    if(!metaDir.exists()) {
      metaDir.mkdirs();
    }
  }

  @Test
  public void processDirTest() throws Exception {
    proc.processDir("./target/meta-test");
    
    for(ConfigProcessor processor : processors) {
      assertTrue("Failed to process one", ((DummyConfigProcessor)processor).processed && !((DummyConfigProcessor)processor).live);
    }
  }
}
