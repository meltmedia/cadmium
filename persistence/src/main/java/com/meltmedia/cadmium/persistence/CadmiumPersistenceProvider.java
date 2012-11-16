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
package com.meltmedia.cadmium.persistence;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.persistence.spi.ProviderUtil;
import javax.sql.DataSource;

import org.hibernate.ejb.HibernatePersistence;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a thin wrapper around org.hibernate.ejb.HibernatePersistence 
 * in order to dynamically configure the Entity classes.
 * 
 * @author John McEntire
 *
 */
public class CadmiumPersistenceProvider implements PersistenceProvider, Closeable {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  /**
   * The instance of HibernatePersistence that is wrapped.
   */
  protected HibernatePersistence wrappedPersistenceProvider = new HibernatePersistence();
  
  /**
   * The list of entity classes that were dynamically found using Reflections.
   */
  protected List<String> entityClassNames = null;
  
  /**
   * This holds a reference to all created entity manager factory instances in order to clean up.
   */
  protected List<EntityManagerFactory> entityManagerFactories = new ArrayList<EntityManagerFactory>();
  
  /**
   * Find all entity classes with Reflections.
   */
  public CadmiumPersistenceProvider() {
    List<String> eClassNames = new ArrayList<String>();
    
    Reflections reflections = new Reflections("com.meltmedia.cadmium");
    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    if(entities != null) {
      for(Class<?> entity : entities) {
        log.debug("Registering entity {}", entity.getName());
        eClassNames.add(entity.getName());
      }
    }
    
    entityClassNames = Collections.unmodifiableList(eClassNames);
  }
  

  /**
   * Uses Hibernate's Reflections and the properties map to override defaults to create a 
   * EntityManagerFactory without the need of any persistence.xml file.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public EntityManagerFactory createEntityManagerFactory(String emName, Map properties) {
    PersistenceUnitInfo pUnit = new CadmiumPersistenceUnitInfo() ;
    return createContainerEntityManagerFactory(pUnit, properties);
  }

  /**
   * This just delegates to the HibernatePersistence method.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public EntityManagerFactory createContainerEntityManagerFactory(
      PersistenceUnitInfo info, Map properties) {
    EntityManagerFactory factory = wrappedPersistenceProvider.createContainerEntityManagerFactory(info, properties);
    entityManagerFactories.add(factory);
    return factory;
  }

  /**
   * This just delegates to the HibernatePersistence method.
   */
  @Override
  public ProviderUtil getProviderUtil() {
    return wrappedPersistenceProvider.getProviderUtil();
  }
  
  /**
   * This class will replace a basic default settings persistence.xml
   * 
   * @author John McEntire
   *
   */
  public class CadmiumPersistenceUnitInfo implements PersistenceUnitInfo {

    private Properties defaultProperties = new Properties();
    
    public CadmiumPersistenceUnitInfo() {
      defaultProperties.setProperty("hibernate.hbm2ddl.auto", "update");
    }
    
    @Override
    public String getPersistenceUnitName() {
      return PersistenceModule.JPA_UNIT_NAME;
    }

    @Override
    public String getPersistenceProviderClassName() {
      return HibernatePersistence.class.getName();
    }

    @Override
    public DataSource getJtaDataSource() {
      return null;
    }

    @Override
    public DataSource getNonJtaDataSource() {
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getMappingFileNames() {
      return Collections.EMPTY_LIST;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<URL> getJarFileUrls() {
      return Collections.EMPTY_LIST;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
      return this.getClass().getProtectionDomain().getCodeSource().getLocation();
    }

    @Override
    public List<String> getManagedClassNames() {
      return entityClassNames;
    }

    @Override
    public boolean excludeUnlistedClasses() {
      return false;
    }

    @Override
    public Properties getProperties() {
      return defaultProperties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
      return "2.0";
    }

    @Override
    public ClassLoader getClassLoader() {
      return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
      
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
      return getClassLoader();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
      return PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
      return SharedCacheMode.UNSPECIFIED;
    }

    @Override
    public ValidationMode getValidationMode() {
      return ValidationMode.AUTO;
    }
    
  }

  @Override
  public void close() throws IOException {
    for(EntityManagerFactory factory : entityManagerFactories) {
      try {
        factory.close();
      } catch(Throwable t){}
    }
    entityManagerFactories = null;
  }

}
