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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.persist.PersistService;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.Reflections;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test for JPA Persistence in Guice module.
 * 
 * @author John McEntire
 *
 */
public class CadmiumPersistenceProviderTest {
  private static PersistService service;
  private static Injector injector;
  
  /**
   * Sets up a jndi naming factory for this test case.
   * 
   * @throws Exception
   */
  @BeforeClass
  public static void setUpClass() throws Exception {
    CadmiumPersistenceProvider.reflections = new Reflections("com.meltmedia.cadmium");
    // Create initial context
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
        "org.apache.naming.java.javaURLContextFactory");
    System.setProperty(Context.URL_PKG_PREFIXES, 
        "org.apache.naming");            
    InitialContext ic = new InitialContext();

    ic.createSubcontext("java:");
    ic.createSubcontext("java:/comp");
    ic.createSubcontext("java:/comp/env");
    
    // Construct DataSource
    JDBCDataSource ds = new JDBCDataSource();
    ds.setUrl("jdbc:hsqldb:mem:testdb");
    ds.setUser("sa");
    ds.setPassword("");
    
    ic.bind("java:/comp/env/DataSource", ds);
    
    setupEMF();
      
  }
  
  /**
   * Sets up a guice injector for this test.
   * 
   * @throws Exception
   */
  private static void setupEMF() throws Exception {
    injector = Guice.createInjector(new PersistenceModule(), new AbstractModule(){

      /**
       * Binds {@link TestEntityDAO}.
       */
      @Override
      protected void configure() {
        bind(TestEntityDAO.class);
      }
      
    });
    Properties properties = injector.getInstance(Key.get(Properties.class, CadmiumJpaProperties.class));
    properties.setProperty("javax.persistence.nonJtaDataSource", "java:/comp/env/DataSource");
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    service = injector.getInstance(PersistService.class);
    service.start();
  }
  
  /**
   * Field used for testing guice injection from test method {@link CadmiumPersistenceProviderTest#testGuiceCreatingEntityManagerFactory()}
   */
  @Inject
  protected EntityManagerFactory emf = null;
  
  /**
   * Tests if guice gets bound correctly with the {@link PersistenceModule}.
   * 
   * @throws Exception
   */
  @Test
  public void testGuiceCreatingEntityManagerFactory() throws Exception {
    injector.injectMembers(this);
    assertNotNull("EntityManagerFactory not injected.", emf);
  }
  
  /**
   * Tests using a simple DAO that was just created for this test. This is a basic CRUD test.
   * 
   * @throws Exception
   */
  @Test
  public void useTestEntity() throws Exception {
    TestEntityDAO dao = injector.getInstance(TestEntityDAO.class);
    TestEntity entity = dao.get(1l);
    assertNull("No Entity should have been found.", entity);
    entity = new TestEntity();
    entity.setCreated(new Date());
    entity.setFlag(false);
    entity.setName("test");
    entity.setInts(new HashSet<Integer>());
    entity.getInts().add(1);
    dao.persistEntity(entity);
    assertNotNull("Entity not persisted.", entity.getId());
    TestEntity entity2 = dao.get(entity.getId());
    validateEntitiesEqual(entity, entity2);
    entity2.setFlag(true);
    entity2.getInts().add(2);
    entity2.setName("Test2");
    dao.updateEntity(entity2);
    assertEquals("Updated dao does not have the same id.", entity.getId(), entity2.getId());
    TestEntity entity3 = dao.get(entity.getId());
    validateEntitiesEqual(entity2, entity3);
    dao.deleteEntity(entity3);
    TestEntity entity4 = dao.get(entity.getId());
    assertNull("Entity should have been deleted.", entity4);
  }

  /**
   * Validates that 2 {@link TestEntity} instances are equal.
   * 
   * @param entity
   * @param entity2
   */
  private void validateEntitiesEqual(TestEntity entity, TestEntity entity2) {
    assertNotNull("Entity not retrieved.", entity2);
    assertEquals("Entity id ["+entity.getId()+"] not equal.", entity.getId(), entity2.getId());
    assertEquals("Entity name ["+entity.getName()+"] not equal.", entity.getName(), entity2.getName());
    assertEquals("Entity create ["+entity.getCreated()+"] not equal.", entity.getCreated(), entity2.getCreated());
    assertEquals("Entity flag ["+entity.getFlag()+"] not equal.", entity.getFlag(), entity2.getFlag());
    assertEquals("Entity ints ["+entity.getInts()+"] not equal.", entity.getInts(), entity2.getInts());
  }
  
  /**
   * Stops the PersistService.
   */
  @AfterClass
  public static void tearDownEMF() {
    if(service != null) {
      service.stop();
    }
  }
}
