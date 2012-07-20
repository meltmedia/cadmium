package com.meltmedia.cadmium.email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Instances of this interface are used to transform messages just before they are passed to the JavaMail API.
 */
public interface MessageTransformer
{
  /**
   * Transforms a message.  The message itself may be changed by this method, or a new message object may be
   * created.
   */
  public MimeMessage transform( MimeMessage message )
    throws MessagingException;
}
