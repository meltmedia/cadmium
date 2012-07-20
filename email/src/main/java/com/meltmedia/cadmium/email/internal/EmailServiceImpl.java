package com.meltmedia.cadmium.email.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.email.Email;
import com.meltmedia.cadmium.email.EmailConnection;
import com.meltmedia.cadmium.email.EmailConnectionImpl;
import com.meltmedia.cadmium.email.EmailException;
import com.meltmedia.cadmium.email.EmailService;
import com.meltmedia.cadmium.email.MessageTransformer;
import com.meltmedia.cadmium.email.SessionStrategy;

/**
 * Email service implementation based on the cadmium email
 * For configuration please see etc/features.xml
 * 
 * @author jkennedy
 * @author bbarr
 * @author chaley
 * @author jmcentire
 */
public class EmailServiceImpl implements EmailService {
  /** The logger for the Email Service */
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  /** The default session strategy class name */
  public static final String DEFAULT_SESSION_STRATEGY = "com.meltmedia.cadmium.email.InitialSessionStrategy";
  
  /** The default message transformer class name */
  public static final String DEFAULT_MESSAGE_TRANSFORMER = "com.meltmedia.cadmium.email.IdentityMessageTransformer";
  
  /** The name of the service property for setting the class name to use for the Session Strategy */
  public static final String SESSION_STRATEGY_CLASS = "melt.mail.sessionstrategy";
  
  /** The name of the service property for setting the class name to be used for the message transformer */
  public static final String MESSAGE_TRANSFORMER_CLASS = "melt.mail.messagetransformer";
    
  /** The Session Strategy for this service instance */
  protected SessionStrategy sessionStrategy = null;
  
  /** The Message Transformer for this service instance */
  protected MessageTransformer messageTransformer = null;
  
  public EmailServiceImpl() {
  	
  }

  /**
   * Activates the Email Service component.
   * 
   * @param context the component context for this component.
   */
  @Inject
  @SuppressWarnings("unchecked")
  //@Activate
  public EmailServiceImpl(@Named("com.meltmedia.email.jndi") String jndiName,@Named(MESSAGE_TRANSFORMER_CLASS) String transformerClassName,@Named(SESSION_STRATEGY_CLASS) String sessionClassName) {
    // Need to get the Session Strategy and Transform class names out of the 
    // Dictionary, and then use reflection to use them
    Dictionary<String,Object> props = new Hashtable<String,Object>();
    props.put("com.meltmedia.email.jndi",jndiName);
    
    try {
      Class<SessionStrategy> ssc = (Class<SessionStrategy>) Class.forName(sessionClassName);
      
      SessionStrategy strategy = ssc.newInstance();
      strategy.configure(props);
      this.sessionStrategy = strategy;
      
      Class<MessageTransformer> mtc = (Class<MessageTransformer>) Class.forName(transformerClassName);
      
      MessageTransformer transformer = mtc.newInstance();
      this.messageTransformer = transformer;
      
     // log.info("Service Started {}", context.getProperties());
      
    } catch (Exception e) {
      throw new RuntimeException("Error Registering Mail Service", e);
    }
  }

  /**
   * Opens a connection, sends the email, then closes the connection
   * 
   * @param email the email to send
   */
  public void send(Email email) throws EmailException {
    synchronized (this) {
      EmailConnection connection = openConnection();
      connection.connect();
      
      connection.send(email);
      
      connection.close();
    }
  }
  
  /**
   * Set a new session strategy
   * 
   * @param sessionStrategy the session strategy to use
   */
  public void setSessionStrategy( SessionStrategy sessionStrategy )
  {
    if( sessionStrategy == null ) {
      throw new  IllegalArgumentException("The session strategy cannot be null.");
    }
    synchronized( this ) {
      this.sessionStrategy = sessionStrategy;
    }
  }

  /**
   * Set a new message transformer
   * 
   * @param messageTransformer the message transformer to use
   */
  public void setMessageTransformer( MessageTransformer messageTransformer )
  {
    if( messageTransformer == null ) {
      throw new IllegalArgumentException("The message transformer cannot be null.");
    }

    synchronized( this ) {
      this.messageTransformer = messageTransformer;
    }
  }

  /**
   * Open a connection using the current session strategy and transformer
   */
  public EmailConnection openConnection()
    throws EmailException
  {
    synchronized( this ) {
      // create a new email connection.
      return new EmailConnectionImpl(sessionStrategy.getSession(), messageTransformer);
    }
  }

  /**
   * Empty session strategy
   * 
   * @author Josh Kennedy
   *
   */
  public static class InitialSessionStrategy
    implements SessionStrategy
  {
    public Session getSession()
      throws EmailException
    {
      throw new EmailException("There is no strategy defined for creating mail sessions.");
    }

    public void configure(Dictionary<String, Object> config)
        throws EmailException {
    }
  }
}
