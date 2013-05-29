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

import com.meltmedia.cadmium.captcha.CaptchaRequest;
import com.meltmedia.cadmium.captcha.CaptchaValidator;
import com.meltmedia.cadmium.core.config.ConfigManager;
import com.meltmedia.cadmium.core.config.ConfigurationListener;
import com.meltmedia.cadmium.email.*;
import com.meltmedia.cadmium.email.config.EmailComponentConfiguration;
import com.meltmedia.cadmium.email.config.EmailConfiguration;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Email service implementation based on the cadmium email
 * For configuration please see etc/features.xml
 * 
 * @author jkennedy
 * @author bbarr
 * @author chaley
 * @author jmcentire
 */
public class EmailServiceImpl implements EmailService, ConfigurationListener<EmailConfiguration> {
  /** The logger for the Email Service */
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  /** The default message transformer class name */
  public static final String DEFAULT_MESSAGE_TRANSFORMER = "com.meltmedia.cadmium.email.IdentityMessageTransformer";
  
  @Inject
  protected ConfigManager configManager = null;
  
  protected EmailConfiguration config = null;
  
  protected EmailSetup setup = null;
  
  protected CaptchaValidator captchaValidator = null;
  
  public EmailServiceImpl() {
  	log.debug("Initialized EmailService...");
  	configurationNotFound();
  }

  /**
   * Updates the Email Service component configuration.
   * 
   */
  public void configurationUpdated(Object emailConfig) {
      EmailConfiguration config = (EmailConfiguration) emailConfig;
      if(config != null && (this.config == null || !config.equals(this.config))) {
        log.info("Updating configuration for email.");
        this.config = config;
      
        //Set the captcha validator up.
        if(!StringUtils.isEmptyOrNull(config.getCaptchaPrivateKey())) {
          log.info("Setting up captcha validation: "+config.getCaptchaPrivateKey());
          captchaValidator = new CaptchaValidator(config.getCaptchaPrivateKey());
        } else {
          log.info("Unsetting captcha validation.");
          captchaValidator = null;
        }
        // Need to get the Session Strategy and Transform class names out of the 
        // Dictionary, and then use reflection to use them
        Dictionary<String,Object> props = new Hashtable<String,Object>();
        if(!StringUtils.isEmptyOrNull(config.getJndiName())) {
          props.put("com.meltmedia.email.jndi",config.getJndiName());
          log.debug("Using jndiName: "+config.getJndiName());
        }
        log.debug("Using {} as the default from address.", config.getDefaultFromAddress());
        if(StringUtils.isEmptyOrNull(config.getSessionStrategy())) {
          config.setSessionStrategy(null);
        }
        log.debug("Using mail session strategy: " + config.getSessionStrategy());
        if(StringUtils.isEmptyOrNull(config.getMessageTransformer())) {
          config.setMessageTransformer(DEFAULT_MESSAGE_TRANSFORMER);
        }
        log.debug("Using message transformer: " + config.getMessageTransformer());
        EmailSetup newSetup = new EmailSetup();
        SessionStrategy strategy = null;
        MessageTransformer transformer = null;
        try {
          if(config.getSessionStrategy() != null) {
            strategy = setSessionStrategy(config, props, newSetup);
          }
        } catch (Exception e) {
          log.error("Error Registering Mail Session Strategy "+config.getSessionStrategy(), e);
          config.setSessionStrategy(null);
        }
        try {  
          transformer = setMessageTransformer(config, newSetup);
        } catch(Exception e) {
          log.error("Error Registering Mail Session Strategy "+config.getSessionStrategy(), e);
          config.setMessageTransformer(DEFAULT_MESSAGE_TRANSFORMER);
          try {
            transformer = setMessageTransformer(config, newSetup);
          } catch(Exception e1) {
            log.error("Failed to fall back to default message transformer.", e1);
          }
        }
        
        this.setup = newSetup;
        
        log.debug("Using new config jndi {}, strategy {}, transformer {}", new Object[] {config.getJndiName(), strategy, transformer});
      }
  }

  /**
   * Instantiates and sets up a new {@link MessageTransformer} instance and assigns a reference to it in the new setup object passed in.
   * 
   * @param config The configuration to pull the class name for the class to instantiate.
   * @param newSetup The {@link EmailSetup} reference to assign the new instance to.
   * @return The new instance of the {@link MessageTransformer} class requested.
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @SuppressWarnings("unchecked")
  private MessageTransformer setMessageTransformer(EmailConfiguration config,
      EmailSetup newSetup) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    MessageTransformer transformer;
    Class<?> mtcCandidate = Class.forName(config.getMessageTransformer());
    
    if(MessageTransformer.class.isAssignableFrom(mtcCandidate)){     
      transformer = ((Class<MessageTransformer>)mtcCandidate).newInstance();
      newSetup.messageTransformer = transformer;
    } else {
      throw new IllegalArgumentException(config.getMessageTransformer() + " is not an instance of " + MessageTransformer.class.getName());
    }
    return transformer;
  }

  /**
   * Instantiates and sets up a new {@link SessionStrategy} instance and assigns a reference to it in the new setup object passed in.
   * 
   * @param config The configuration to pull the class name for the class to instantiate.
   * @param props The properties to configure the new {@link SessionStrategy} with.
   * @param newSetup The {@link EmailSetup} reference to assign the new instance to.
   * @return The new instance of the {@link SessionStrategy} class requested.
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws EmailException
   * @throws IllegalAccessException
   */
  @SuppressWarnings("unchecked")
  private SessionStrategy setSessionStrategy(EmailConfiguration config,
      Dictionary<String, Object> props, EmailSetup newSetup)
      throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, EmailException, IllegalAccessException {
    SessionStrategy strategy;
    Class<?> ssc = Class.forName(config.getSessionStrategy());
    
    if(SessionStrategy.class.isAssignableFrom(ssc)) {
      strategy = ((Class<SessionStrategy>)ssc).newInstance();
      strategy.configure(props);
      newSetup.sessionStrategy = strategy;
    } else {
      throw new IllegalArgumentException(config.getSessionStrategy() + " is not an instance of " + SessionStrategy.class.getName());
    }
    return strategy;
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
   * Open a connection using the current session strategy and transformer
   */
  public EmailConnection openConnection()
    throws EmailException
  {
    
    // create a new email connection.
    return new EmailConnectionImpl(setup.sessionStrategy.getSession(), setup.messageTransformer);
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
  
  /**
   * An Object to hold staged instances of the {@link SessionStrategy} and the 
   * {@link MessageTransformer} so that the switching of their references will be atomic.
   * 
   * @author John McEntire
   *
   */
  public static class EmailSetup {
      
    /** The Session Strategy for this service instance */
    SessionStrategy sessionStrategy = null;
    
    /** The Message Transformer for this service instance */
    MessageTransformer messageTransformer = null;
    
  }

  /**
   * Sets default configurations on this {@link EmailService}.
   */
  @Override
  public void configurationNotFound() {
    log.warn("No email configuration found.");
    EmailConfiguration defaultConfiguration = new EmailConfiguration();
    defaultConfiguration.setMessageTransformer(DEFAULT_MESSAGE_TRANSFORMER);
    defaultConfiguration.setSessionStrategy(null);
    
    configurationUpdated(defaultConfiguration);
  }
  
  public String getFromAddress(String fromAddress) {
  	return StringUtils.isEmptyOrNull(fromAddress) ? config.getDefaultFromAddress() : fromAddress;
  }
  
  public boolean validateCaptcha(HttpServletRequest request, CaptchaRequest captcha, EmailComponentConfiguration compConfig, Logger log) {
  	log.info("EmailComponentConfiguration = {}", compConfig.toString());
    if(captchaValidator != null && compConfig != null && request != null && compConfig.getUseCaptcha() != null && compConfig.getUseCaptcha()) {
      return captchaValidator.isValid(request, captcha);
    }
    return true;
  }

  public boolean validateCaptcha(HttpServletRequest request, CaptchaRequest captcha, EmailComponentConfiguration compConfig) {
    return validateCaptcha(request, captcha, compConfig, log);
  }
}
