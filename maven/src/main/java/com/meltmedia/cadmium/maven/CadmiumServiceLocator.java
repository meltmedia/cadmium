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

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.slf4j.Logger;
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.impl.internal.Slf4jLogger;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;

/**
 * This pulls together the aether-impl and maven-aether-impl DefaultServiceLocators.
 * 
 * @author John McEntire
 *
 */
public class CadmiumServiceLocator extends DefaultServiceLocator {

  /**
   * Adds Services not included in the aether-impl. 
   * 
   * @param log To configure Slf4j logging.
   */
  public CadmiumServiceLocator(Logger log) {
    super();
    if(log != null) {
      setServices( org.sonatype.aether.spi.log.Logger.class, new Slf4jLogger(log) );
    }
    addService( ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class );
    addService( VersionResolver.class, DefaultVersionResolver.class );
    addService( VersionRangeResolver.class, DefaultVersionRangeResolver.class );
    addService( MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class );
    addService( MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class );
    
    // Adding connectors
    addService( RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class );
    addService( RepositoryConnectorFactory.class, AsyncRepositoryConnectorFactory.class );
  }
}
