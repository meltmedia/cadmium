package com.meltmedia.cadmium.maven;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ArtifactResolverTest {

  @Test
  public void testResolveArtifact() throws Exception {
    File localRepo = new File("./target/.m2");
    if(localRepo.exists()) {
      FileUtils.cleanDirectory(localRepo);
    }
    FileUtils.forceMkdir(localRepo);
    ArtifactResolver resolver = new ArtifactResolver(null, localRepo.getAbsolutePath());
    File artifact = resolver.resolveMavenArtifact("org.apache.maven:maven-artifact:3.0.4");
    System.err.println("Artifact is downloaded to: " + artifact.getAbsolutePath());
    assertTrue("Artifact not downloaded.", artifact.exists());
    assertTrue("Artifact cannot be read.", artifact.canRead());
    assertTrue("Artifact points to empty file.", artifact.lastModified() > 0l);
  }
}
