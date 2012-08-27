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
package com.meltmedia.cadmium.email;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class TestingMessageTransformer
  implements MessageTransformer
{
  private static final Logger log = LoggerFactory.getLogger(TestingMessageTransformer.class);

  /** The system property that turns message logging on and off. {@value} */
  public static final String LOG_SYSTEM_PROPERTY = "com.meltmedia.email.test.log";

  /** The system property that turns message sending on and off. {@value} */
  public static final String SEND_SYSTEM_PROPERTY = "com.meltmedia.email.test.send";

  /** The system property for test email to address. {@value} */
  public static final String TO_SYSTEM_PROPERTY = "com.meltmedia.email.test.to";

  /** The system property for test email to address. {@value} */
  public static final String FROM_SYSTEM_PROPERTY = "com.meltmedia.email.test.from";

  /** The system property for test email subjects. {@value} */
  public static final String SUBJECT_SYSTEM_PROPERTY = "com.meltmedia.email.test.subject";

  /** {@value} */
  public static final boolean LOG_DEFAULT_VALUE = true;

  /** {@value} */
  public static final boolean SEND_DEFAULT_VALUE = false;

  /** {@value} */
  public static final String TO_DEFAULT_VALUE = "test@meltmedia.com";

  /** {@value} */
  public static final String FROM_DEFAULT_VALUE = "test@meltmedia.com";

  /** {@value} */
  public static final String SUBJECT_DEFAULT_VALUE = "[TEST]";

  public static boolean getLogProperty()
  {
    return getBooleanSystemProperty(LOG_SYSTEM_PROPERTY, LOG_DEFAULT_VALUE);
  }

  public static boolean getSendProperty()
  {
    return getBooleanSystemProperty(SEND_SYSTEM_PROPERTY, SEND_DEFAULT_VALUE);
  }

  public static String getToProperty()
  {
    return getStringSystemProperty(TO_SYSTEM_PROPERTY, TO_DEFAULT_VALUE);
  }

  public static String getFromProperty()
  {
    return getStringSystemProperty(FROM_SYSTEM_PROPERTY, FROM_DEFAULT_VALUE);
  }

  public static String getSubjectProperty()
  {
    return getStringSystemProperty(SUBJECT_SYSTEM_PROPERTY, SUBJECT_DEFAULT_VALUE);
  }

  public static boolean getBooleanSystemProperty( String systemProperty, boolean defaultValue )
  {
    String propertyValue = System.getProperty(systemProperty);
    return propertyValue != null ? Boolean.valueOf(propertyValue).booleanValue() : defaultValue;
  }

  public static String getStringSystemProperty( String systemProperty, String defaultValue )
  {
    String propertyValue = System.getProperty(systemProperty);
    return propertyValue != null ? propertyValue : defaultValue;
  }


  public MimeMessage transform( MimeMessage message )
    throws MessagingException
  {
    MimeBodyPart sentToBodyPart = newSentToBodyPart( message );
    MimeBodyPart originalBodyPart = newOriginalBodyPart( message );

    // create a new multipart content for this message.
    MimeMultipart multipart = new MimeMultipart(EmailUtil.SUBTYPE_MIXED);

    // add the parts to the body.
    multipart.addBodyPart(originalBodyPart);
    multipart.addBodyPart(sentToBodyPart);

    // get the new values for all of the headers.
    InternetAddress newFromAddress = newFromAddress(message);
    InternetAddress[] newToAddresses = newToAddresses(message);
    InternetAddress[] newCcAddresses = newCcAddresses(message);
    InternetAddress[] newBccAddresses = newBccAddresses(message);
    String newSubject = newSubject(message);

    // update the message.
    message.setFrom(newFromAddress);
    message.setRecipients(Message.RecipientType.TO, newToAddresses);
    message.setRecipients(Message.RecipientType.CC, newCcAddresses);
    message.setRecipients(Message.RecipientType.BCC, newBccAddresses);
    message.setSubject(newSubject);
    message.setContent(multipart);

    // save the message.
    message.saveChanges();

    if( getLogProperty() ) {
      try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      message.writeTo(out);
      log.info("Email Message Sent:\n{}", out.toString());
      }
      catch( IOException ioe ) {
        throw new MessagingException("Exception thrown while writing message to log.", ioe);
      }
    }
    if( !getSendProperty() ) {
      message = null;
    }

    // return the message.
    return message;
  }

  public InternetAddress newFromAddress( MimeMessage message )
    throws MessagingException
  {
    return new InternetAddress(getFromProperty());
  }

  public InternetAddress[] newToAddresses( MimeMessage message )
    throws MessagingException
  {
    return new InternetAddress[] {new InternetAddress(getToProperty())};
  }

  public InternetAddress[] newCcAddresses( MimeMessage message )
    throws MessagingException
  {
    return new InternetAddress[] {};
  }

  public InternetAddress[] newBccAddresses( MimeMessage message )
    throws MessagingException
  {
    return new InternetAddress[] {};
  }

  public String newSubject( MimeMessage message )
    throws MessagingException
  {
    return getSubjectProperty()+message.getSubject();
  }

  public MimeBodyPart newSentToBodyPart( MimeMessage message )
    throws MessagingException
  {
    // get information about the original message.
    Address[] originalFromRecipient = message.getFrom();
    Address[] originalToRecipient = message.getRecipients(Message.RecipientType.TO);
    Address[] originalCcRecipient = message.getRecipients(Message.RecipientType.CC);
    Address[] originalBccRecipient = message.getRecipients(Message.RecipientType.BCC);
    String originalSubject = message.getSubject();

    // create the html for the string buffer.
    StringBuffer html = new StringBuffer();
    html.append("<html><body><table style=\"width: 100%;\"><tr><td>header</td><td>value</td></tr>");
    html.append("<tr><td>Subject:</td><td>").append(escape(originalSubject)).append("</td></tr>");

    // iterate over the addresses.
    if( originalFromRecipient != null ) {
      for( int i = 0; i < originalFromRecipient.length; i++ ) {
        html.append("<tr><td>FROM:</td><td>").append(escape(originalFromRecipient[i])).append("</td></tr>");
      }
    }
    if( originalToRecipient != null ) {
      for( int i = 0; i < originalToRecipient.length; i++ ) {
        html.append("<tr><td>TO:</td><td>").append(escape(originalToRecipient[i])).append("</td></tr>");
      }
    }
    if( originalCcRecipient != null ) {
      for( int i = 0; i < originalCcRecipient.length; i++ ) {
        html.append("<tr><td>CC:</td><td>").append(escape(originalCcRecipient[i])).append("</td></tr>");
      }
    }
    if( originalBccRecipient != null ) {
      for( int i = 0; i < originalBccRecipient.length; i++ ) {
        html.append("<tr><td>BCC:</td><td>").append(escape(originalBccRecipient[i])).append("</td></tr>");
      }
    }
    html.append("</table></body></html>");

    MimeBodyPart sentToBodyPart = new MimeBodyPart();
    sentToBodyPart.setContent(html.toString(), "text/html");
    sentToBodyPart.setHeader("Content-ID", "original-addresses");
    sentToBodyPart.setDisposition("inline");

    return sentToBodyPart;
  }
  
  /**
   * Executes toString() on the given object and returns the string properly escaped for use in 
   * the original message information section of an email. The string will be properly HTML 
   * escaped and will be replaced with the empty string if null. 
   */
  protected String escape(Object o) {
    return escapeHtml(defaultToString(o));
  }
  
  private static String defaultToString(Object o) {
    return o == null? "" : o.toString() == null? "" : o.toString(); 
  }

  public MimeBodyPart newOriginalBodyPart( MimeMessage message )
    throws MessagingException
  {
    // get the data handler for this message.
    DataHandler handler = message.getDataHandler();

    // add the original body to the new body part.
    MimeBodyPart originalPart = new MimeBodyPart();
    originalPart.setDataHandler(handler);
    originalPart.setHeader("Content-ID", "original-message");

    return originalPart;
  }
}
