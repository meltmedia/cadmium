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
