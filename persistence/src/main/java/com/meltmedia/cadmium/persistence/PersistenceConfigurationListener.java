package com.meltmedia.cadmium.persistence;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.PersistService;
import com.meltmedia.cadmium.core.config.ConfigurationListener;

/**
 * Listens for persistence configuration changes and manages the Jpa Service.
 * 
 * @author John McEntire
 *
 */
@Singleton
public class PersistenceConfigurationListener implements
    ConfigurationListener<PersistenceConfiguration>, Closeable {
  private static final Logger log = LoggerFactory.getLogger(PersistenceConfigurationListener.class);
  
  /*public PersistenceConfigurationListener() {
    try {
      PersistenceConfigurationListener.init(Collections.list(Thread.currentThread().getContextClassLoader().getResources("/")));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }*/
  
  /*
   * Creates the persistence xml dynamically in order to work around jpa not 
   * having a way to dynamically add entities on the fly.
   *
  public static void init(Collection<URL> metaDirUrls) throws IOException {
    log.info("Started initializing persistence xml.");
    VelocityEngine engine = new VelocityEngine();
    VelocityContext context = new VelocityContext();
    
    try {
      engine.init();
    } catch (Exception e) {
      log.error("Failed to initialize jpa persistence xml dynamically.", e);
      throw new RuntimeException("Failed to initialize jpa persistence xml dynamically.", e);
    }
    
    Reflections reflections = new Reflections("com.meltmedia.cadmium");
    Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
    if(entities != null) {
      
      context.put("entityList", entities);
    } else {
      context.put("entityList", new HashSet<Class<?>>());
    }

    boolean foundOne = false;
    try {
      String template = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("persistence.xml.template"));
      StringWriter writer = new StringWriter();
      
      engine.evaluate(context, writer, "persistence.xml", template);
      File metaDirToUse = null;
      for(URL metaDirUrl : metaDirUrls) {
        File metaDir = null;
        log.debug("Checking Classpath URL: {}", metaDirUrl);
        if (metaDirUrl.getProtocol().equals("file")) {
          metaDir = new File(metaDirUrl.getFile());
        } else if (metaDirUrl.getProtocol().startsWith("vfs")) {
          metaDir = new File(new JBossVfsUrlType().createDir(metaDirUrl).getPath());
        }
        
        if(metaDir != null && metaDir.canWrite() && metaDir.getName().endsWith("classes")) {
          metaDirToUse = new File(metaDir, "META-INF");
        }
      }

      if(metaDirToUse != null && (metaDirToUse.canWrite() || (!metaDirToUse.exists() && metaDirToUse.mkdirs() && metaDirToUse.canWrite()))) {
        File persistenceXmlFile = new File(metaDirToUse, "persistence.xml");
        log.debug("Writing a new peristence.xml file to {}", metaDirToUse.getAbsoluteFile().getAbsolutePath());
        FileUtils.write(persistenceXmlFile, writer.toString(), false);
        foundOne = true;
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to create jpa persistence.xml file dynamically.", e);
    }
    if(!foundOne) {
      throw new IOException("Failed to dynamically create a persistence.xml on the classpath.");
    }
    log.info("Done initializing persistence xml.");
  }*/
  
  @CadmiumJpaProperties
  protected Properties jpaOverrideProperties;
  
  @Inject
  protected PersistService jpaService;

  @Override
  public void configurationUpdated(Object configuration) {
    log.debug("received a new configuration for the jpa persistence service.");
    PersistenceConfiguration cfg = (PersistenceConfiguration) configuration;
    Properties updatedProps = new Properties();
    if(!StringUtils.isEmptyOrNull(cfg.getJndiName())) {
      updatedProps.setProperty("javax.persistence.nonJtaDataSource", cfg.getJndiName());
      log.debug("JNDIName: "+cfg.getJndiName());
    }
    if(!StringUtils.isEmptyOrNull(cfg.getDatabaseDialect())) {
      updatedProps.setProperty("hibernate.dialect", cfg.getDatabaseDialect());
      log.debug("Dialect: "+cfg.getDatabaseDialect());
    }
    if(cfg.getExtraConfiguration() != null) {
      updatedProps.putAll(cfg.getExtraConfiguration());
      log.debug("Extra Config: "+cfg.getExtraConfiguration());
    }
    Properties oldJpaProperties = new Properties();
    oldJpaProperties.putAll(jpaOverrideProperties);
    if(!jpaOverrideProperties.equals(updatedProps)) { 
      try {
        try {
          log.debug("Stopping jpa service...");
          jpaService.stop();
        } catch(Throwable t) {
          log.debug("Jpa service has already been stopped.", t);
        }
        log.debug("Setting override properties to new values.");
        jpaOverrideProperties.clear();
        jpaOverrideProperties.putAll(updatedProps);
        log.debug("Starting jpa service...");
        jpaService.start();
        log.debug("Jpa service is up and running.");
      } catch(Throwable t) {
        log.error("Failed to set new properties into jpa service. Resetting back to older values.", t);
        jpaOverrideProperties.clear();
        jpaOverrideProperties.putAll(oldJpaProperties);
        jpaService.start();
        //throw new Error(t);
      }
    }
  }

  @Override
  public void configurationNotFound() {
    try {
      log.debug("No persistence configuration found. Making sure jpa service is not running.");
      jpaService.stop();
    } catch(Throwable t) {
      log.debug("Jpa service has already been stopped.");
    }
  }

  @Override
  public void close() throws IOException {
    try {
      jpaService.stop();
    } catch(Throwable t) {
      log.debug("Jpa service has already been stopped.");
    }
  }

}
