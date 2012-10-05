package com.meltmedia.cadmium.persistence;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Properties;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.persist.PersistService;

import static org.mockito.Mockito.*;

public class CadmiumPersistenceProviderTest {
  
  @BeforeClass
  public static void setUpClass() throws Exception {
      // rcarver - setup the jndi context and the datasource
      try {
          // Create initial context
          System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
              "org.apache.naming.java.javaURLContextFactory");
          System.setProperty(Context.URL_PKG_PREFIXES, 
              "org.apache.naming");            
          InitialContext ic = new InitialContext();

          ic.createSubcontext("java:");
          ic.createSubcontext("java:/comp");
          ic.createSubcontext("java:/comp/env");
          ic.createSubcontext("java:/comp/env/jdbc");
         
          // Construct DataSource
          DataSource ds = mock(DataSource.class);
          Connection conn = mock(Connection.class);
          DatabaseMetaData meta = mock(DatabaseMetaData.class);
          ResultSet resultSet = mock(ResultSet.class);
          when(meta.getTypeInfo()).thenReturn(resultSet);
          when(meta.getSQLKeywords()).thenReturn("");
          when(conn.getMetaData()).thenReturn(meta);
          when(ds.getConnection()).thenReturn(conn);
          
          ic.bind("java:/comp/env/DataSource", ds);
      } catch (NamingException ex) {
        ex.printStackTrace();
      }
      
  }

  @Test
  public void test() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("javax.persistence.nonJtaDataSource", "java:/comp/env/DataSource");
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(PersistenceModule.JPA_UNIT_NAME, properties);
    assertNotNull("EntityManagerFactory not created.", emf);
  }
  
  @Inject
  protected EntityManagerFactory emf = null;
  
  @Test
  public void testGuice() throws Exception {
    Injector injector = Guice.createInjector(new PersistenceModule());
    Properties properties = injector.getInstance(Key.get(Properties.class, CadmiumJpaProperties.class));
    properties.setProperty("javax.persistence.nonJtaDataSource", "java:/comp/env/DataSource");
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
    PersistService service = injector.getInstance(PersistService.class);
    service.start();
    try {
      injector.injectMembers(this);
      assertNotNull("EntityManagerFactory not injected.", emf);
    } finally {
      service.stop();
    }
  }
}
