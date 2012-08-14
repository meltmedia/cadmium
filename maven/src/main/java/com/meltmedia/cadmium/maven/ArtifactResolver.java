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
package com.meltmedia.cadmium.maven;

import java.io.File;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * <p>A simple way to get a url to download an artifact from a maven artifact vector.</p>
 * 
 * @author John McEntire
 *
 */
public class ArtifactResolver {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  /**
   * The url to the remote maven repository.
   */
  private String remoteMavenRepository = "http://repo1.maven.org/maven2/";
  
  /**
   * The path to the local maven repository.
   */
  private String localRepository;
  
  private ServiceLocator locator;

  /**
   * Sets up a basic ArtifactResolver that is backed by the specified remote maven repository.
   * 
   * @param remoteMavenRepository The url to a remote maven repository.
   */
  public ArtifactResolver(String remoteMavenRepository, String localRepository) {
    if(!StringUtils.isEmptyOrNull(remoteMavenRepository)) {
      this.remoteMavenRepository = remoteMavenRepository;
    }
    this.localRepository = localRepository;
    
    locator = new CadmiumServiceLocator( log );
  }
  
  /**
   * Fetches a maven artifact and returns a File Object that points to its location.
   * 
   * @param artifact The maven coordinates in the format of <code>groupId:artifactId:type:version</code>
   * @return A File Object that points to the artifact.
   * @throws ArtifactResolutionException 
   */
  public File resolveMavenArtifact(String artifact) throws ArtifactResolutionException {
    RepositorySystem repoSystem = newRepositorySystem();
    
    RepositorySystemSession session = newSession( repoSystem );
    
    Artifact artifactObj = new DefaultArtifact( artifact );
    
    RemoteRepository repo = new RemoteRepository( "cadmium-central", "default", remoteMavenRepository );
    
    ArtifactRequest artifactRequest = new ArtifactRequest();
    artifactRequest.setArtifact( artifactObj );
    artifactRequest.addRepository( repo );
    
    ArtifactResult artifactResult = repoSystem.resolveArtifact( session, artifactRequest );
    
    artifactObj = artifactResult.getArtifact();
    
    return artifactObj.getFile();
  }
  
  /**
   * @return A new RepositorySystem. 
   */
  protected RepositorySystem newRepositorySystem() {
    return locator.getService( RepositorySystem.class );
  }
  
  /**
   * Creates a new RepositorySystemSession.
   * @param system A RepositorySystem to get a LocalRepositoryManager from.
   * @return The new instance of a RespositorySystemSession.
   */
  protected RepositorySystemSession newSession( RepositorySystem system )
  {
      MavenRepositorySystemSession session = new MavenRepositorySystemSession();

      LocalRepository localRepo = new LocalRepository( localRepository );
      session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );

      return session;
  }
  
}
