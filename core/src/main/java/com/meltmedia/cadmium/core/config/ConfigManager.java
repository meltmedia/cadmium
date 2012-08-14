package com.meltmedia.cadmium.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.config.impl.PropertiesReaderImpl;
import com.meltmedia.cadmium.core.config.impl.PropertiesWriterImpl;

/**
 * This centralizes and manages how other classes read and write to properties files. 
 * 
 * @author Brian Barr
 */

@Singleton
public class ConfigManager {

  private final Logger log = LoggerFactory.getLogger(ConfigManager.class);
  
  private Properties defaultProperties;    
  private PropertiesReader reader = new PropertiesReaderImpl();
  private PropertiesWriter writer = new PropertiesWriterImpl();

  public Properties getPropertiesByFile(File configFile) {

    /*Properties properties = new Properties();    
    Reader reader = null;
    try{

      log.info("configFile path: {}", configFile.getPath());
      reader = new FileReader(configFile);
      properties.load(reader);     
      
      logProperties(log, properties, configFile.getCanonicalPath());
    }
    catch(Exception e) {

      log.warn("Failed to load "+configFile.getAbsolutePath());
    }
    finally {

      IOUtils.closeQuietly(reader);
    }*/
    
    return reader.getProperties(configFile, log);

  }

  // rework to be loadPropertiesIfExists
  public Properties loadProperties(Properties properties, File configFile) {

    //if( !configFile.exists() /*|| !configFile.canRead()*/) return properties;
    
    /*Reader reader = null;
    try{
      
      log.info("configFile path: {}", configFile.getPath());

      reader = new FileReader(configFile);
      properties.load(reader);
      
      logProperties(log, properties, configFile.getCanonicalPath());
    }
    catch(Exception e) {

      log.warn("Failed to load properties file ["
          + configFile.getAbsolutePath() + "] from content directory.", e);
    }
    finally {

      IOUtils.closeQuietly(reader);
    }*/
    
    return reader.appendProperties(properties, configFile, log);
  }

  public Properties getSystemProperties() {

    Properties properties = new Properties();     
    properties.putAll(System.getenv());
    properties.putAll(System.getProperties());
            
    return properties;

  }

  public Properties getPropertiesByContext(ServletContext context, String path) {

    /*Properties properties = new Properties();
    Reader reader = null;
    try{

      reader = new InputStreamReader(context.getResourceAsStream(path), "UTF-8");
      properties.load(reader);
      
      logProperties(log, properties, path);
    } 
    catch(Exception e) {

      log.warn("Failed to load "+path);
    }
    finally {

      IOUtils.closeQuietly(reader);
    }*/

    return reader.getProperties(context, path, log);
  }
  
  public Properties getPropertiesByFileName(String fileName) {
    
    /*Properties properties = new Properties();
  
    if(new File(fileName).canRead()) {
     
      FileInputStream in = null;
      try {
        
        in = new FileInputStream(fileName);        
        properties.load(in);
      }
      catch(Exception e){
        
        log.warn("Failed to read in properties file.", e);
      } 
      finally {
        IOUtils.closeQuietly(in);
      }
    }*/
    
    return reader.getProperties(fileName, log);
  }
  
  public Properties getPropertiesByPath(Properties properties, String path) throws IOException {
    
    /*FileReader reader = null;
    try {
      
      reader = new FileReader(path);
      properties.load(reader);
    }
    catch(Exception e) {
    
      log.warn("Failed to load in properties for path: {}", path);
    }
    finally {
      IOUtils.closeQuietly(reader);
    }  */
    
    return reader.getProperties(properties, path, log);
  }

  public void persistProperties(Properties properties, String fileName, String message) {

    /*File propsFile = new File(fileName);
    if(propsFile.canWrite() || !propsFile.exists()) {
      
      if(!properties.isEmpty()) {
        
        ensureDirExists(propsFile.getParent());
        FileOutputStream out = null;
        
        try {
          
          out = new FileOutputStream(propsFile);
          properties.store(out, message);
          out.flush();
        } 
        catch(Exception e) {
          
          log.warn("Failed to persist vault properties file.", e);
        } 
        finally {
          IOUtils.closeQuietly(out);
        }
      }
    }*/
    
    writer.persistProperties(properties, fileName, message, log);
    
  }
  
  public void logProperties( Logger log, Properties properties, String name ) {
    if( log.isDebugEnabled() ) {
      StringBuilder sb = new StringBuilder().append(name).append(" properties:\n");
      for(Object key : properties.keySet()) {
        sb.append("  ").append(key.toString()).append(properties.getProperty(key.toString())).append("\n");
      }
      log.debug(sb.toString());
    }
  }  

  public Properties getDefaultProperties() {
    return defaultProperties;
  }

  public void setDefaultProperties(Properties defaultProperties) {
    this.defaultProperties = defaultProperties;
  }

  /**
  * <p>A template for reading resources from a file.</p>
  * <pre>
  * return new ReadFileTemplate<Properties>(file, "UTF-8", log) {
  *   public Properties withReader(Reader reader) throws IOException {
  *     Properties properties = new Properties();
  *     properties.load(reader);
  *     return properties;
  *   }
  * }.read();
  * </pre>
  * 
  * @author Christian Trimble
  */
  public abstract class ReadFileTemplate<T>
  {
    private File file;
    private String encoding;
    private Logger log;

    public ReadFileTemplate( File file, String encoding, Logger log ) {
      this.file = file;
      this.encoding = encoding;
      this.log = log;
    }
    
    public final T read() throws IOException {
      Reader reader = null;
      try {
        reader = new InputStreamReader(new FileInputStream(file), encoding);
        return withReader(reader);
      }
      catch(IOException ioe) {
        log.debug("Failed to read file: {}", file.getPath(), ioe);
        throw ioe;
      }
      finally {
        IOUtils.closeQuietly(reader);
      }  
    }
    
    public abstract T withReader( Reader reader ) throws IOException;
  }
  
  /**
   * <p>A template for reading resources from a servlet context.</p>
   * <pre>
   * return new ReadResourceTemplate<Properties>(servletContext, "/path/to/file", "UTF-8", log) {
   *   public Properties withReader(Reader reader) throws IOException {
   *     Properties properties = new Properties();
   *     properties.load(reader);
   *     return properties;
   *   }
   * }.read();
   * </pre>
   * 
   * @author Christian Trimble
   *
   */
  public abstract class ReadResourceTemplate<T>
  {
    private ServletContext context;
    private String path;
    private String encoding;
    private Logger log;

    public ReadResourceTemplate( ServletContext context, String path, String encoding, Logger log ) {
      this.context = context;
      this.path = path;
      this.encoding = encoding;
      this.log = log;
    }
    
    public final T read() throws IOException {
      Reader reader = null;
      try {
        reader = new InputStreamReader(context.getResourceAsStream(path), encoding);
        return withReader(reader);
      }
      catch(IOException ioe) {
        log.debug("Failed to read resource from context: {}", path, ioe);
        throw ioe;
      }
      finally {
        IOUtils.closeQuietly(reader);
      }  
    }
    
    public abstract T withReader( Reader reader ) throws IOException;
  }

}
