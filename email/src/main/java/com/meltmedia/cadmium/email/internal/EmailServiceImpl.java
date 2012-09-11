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
package com.meltmedia.cadmium.email.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;
import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.config.ConfigurationNotFoundException;
import com.meltmedia.cadmium.email.Email;
import com.meltmedia.cadmium.email.EmailConnection;
import com.meltmedia.cadmium.email.EmailConnectionImpl;
import com.meltmedia.cadmium.email.EmailException;
import com.meltmedia.cadmium.email.EmailService;
import com.meltmedia.cadmium.email.MessageTransformer;
import com.meltmedia.cadmium.email.SessionStrategy;
import com.meltmedia.cadmium.email.config.EmailConfiguration;

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
  
  @Inject
  protected ConfigManager configManager = null;
  
  protected EmailConfiguration config = null;
  
  public EmailServiceImpl() {
  	log.debug("Initialized EmailService...");
  }

  /**
   * Updates the Email Service component configuration.
   * 
   */
  @SuppressWarnings("unchecked")
  public void updateConfiguration() throws EmailException {
    try {
      EmailConfiguration config = configManager.getConfiguration(EmailConfiguration.KEY, EmailConfiguration.class);
      if(config != null && config != this.config) {
        log.info("Updating configuration for email.");
        this.config = config;
      
        // Need to get the Session Strategy and Transform class names out of the 
        // Dictionary, and then use reflection to use them
        Dictionary<String,Object> props = new Hashtable<String,Object>();
        props.put("com.meltmedia.email.jndi",config.getJndiName());
        
        try {
          Class<SessionStrategy> ssc = (Class<SessionStrategy>) Class.forName(config.getSessionStrategy());
          
          SessionStrategy strategy = ssc.newInstance();
          strategy.configure(props);
          this.sessionStrategy = strategy;
          
          Class<MessageTransformer> mtc = (Class<MessageTransformer>) Class.forName(config.getMessageTransformer());
          
          MessageTransformer transformer = mtc.newInstance();
          this.messageTransformer = transformer;
          
          log.debug("Using new config jndi {}, strategy {}, transformer {}", new Object[] {config.getJndiName(), config.getSessionStrategy(), config.getMessageTransformer()});
          
        } catch (Exception e) {
          throw new EmailException("Error Registering Mail Service", e);
        }
      }
    } catch (ConfigurationNotFoundException e) {
      throw new EmailException("ConfigurationNotFoundException: " + e.getMessage(), e);
      
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
      updateConfiguration();
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
