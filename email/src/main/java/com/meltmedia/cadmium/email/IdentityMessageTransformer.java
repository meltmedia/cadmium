package com.meltmedia.cadmium.email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * This message transformer returns the same message that was passed in, without modification.
 */
public class IdentityMessageTransformer
  implements MessageTransformer
{
  public MimeMessage transform( MimeMessage message )
    throws MessagingException
  {
    return message;
  }
}
