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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Base Email Class
 * 
 */
public abstract class Email
{
  protected InternetAddressSet toSet = new InternetAddressSet();
  protected InternetAddressSet ccSet = new InternetAddressSet();
  protected InternetAddressSet bccSet = new InternetAddressSet();
  protected InternetAddress from = null;
  protected InternetAddress replyTo = null;
  protected String subject = null;
  
  /**
   * Helper methods for adding recipients
   */
  public void addTo(String address) {
    addAddressHelper(toSet, address);
  }
  
  public void addCC(String address) {
    addAddressHelper(ccSet, address);
  }
  
  public void addBCC(String address) {
    addAddressHelper(bccSet, address);
  }
  
  /**
   * Checks if the addresses need to be split either on , or ;
   * 
   * @param set Internet address set to add the address to
   * @param address address or addresses to add to the given set
   */
  private void addAddressHelper(InternetAddressSet set, String address) {
    if (address.contains(",") || address.contains(";")) {
      String[] addresses = address.split("[,;]");
      
      for (String a : addresses) {
        set.add(a);
      }
    }
    else {
      set.add(address);
    }
  }
  
  /**
   * Returns the list of to addresses as strings.  If there are no to addresses, then an empty list is returned.
   */
  public InternetAddressSet getToSet()
  {
    return toSet;
  }

  /**
   * Returns the list of cc addresses as strings.  If there are no cc addresses, then an empty list is returned.
   */
  public InternetAddressSet getCcSet()
  {
    return ccSet;
  }

  /**
   * Returns the list of bcc addresses as strings.  If there are not bcc addresses, then an empty list is returned.
   */
  public InternetAddressSet getBccSet()
  {
    return bccSet;
  }

  /**
   * Sets the from address for this message.
   */
  public void setFrom( String from )
  {
    setFrom(convertAddress(from));
  }

  /**
   * Sets the from address for this message.
   */
  public void setFrom( InternetAddress from )
  {
    this.from = from;
  }

  /**
   * Returns the from address for this email.
   */
  public InternetAddress getFrom()
  {
    return this.from;
  }

  /**
   * Sets the replyTo address for this message.
   */
  public void setReplyTo( String replyTo )
  {
    setReplyTo(convertAddress(replyTo));
  }

  /**
   * Sets the replyTo address for this message.
   */
  public void setReplyTo( InternetAddress replyTo )
  {
    this.replyTo = replyTo;
  }

  /**
   * Returns the subject for this message
   * 
   * @return
   */
  public String getSubject()
  {
    return subject;
  }

  /**
   * Sets the subject for this message;
   * 
   * @param subject
   */
  public void setSubject( String subject )
  {
    this.subject = subject;
  }

  /**
   * Simplifies this email by removing duplicate pieces of information.  The standard implementation removes duplicate
   * recipient emails in the to, cc, and bcc sets.
   */
  public void simplify()
  {
    // remove all addresses from the cc and bcc that are in the to address set.
    ccSet.removeAll(toSet);
    bccSet.removeAll(toSet);

    // remove all address from the bcc set that are in the cc set.
    bccSet.removeAll(ccSet);
  }

  /**
   * Populates a mime message with the recipient addresses, from address, reply to address, and the subject.
   */
  protected void populate( MimeMessage message )
    throws MessagingException
  {
    // add all of the to addresses.
    message.addRecipients(Message.RecipientType.TO, toSet.toInternetAddressArray());
    message.addRecipients(Message.RecipientType.CC, ccSet.toInternetAddressArray());
    message.addRecipients(Message.RecipientType.BCC, bccSet.toInternetAddressArray());
    message.setFrom(from);
    if( replyTo != null ) {
      message.setReplyTo(new InternetAddress[] {replyTo});
    }
    if( subject != null ) {
      message.setSubject(subject);
    }
  }

  /**
   * Converts this email message into a java mail message.
   * @throws EmailException 
   */
  public abstract MimeMessage newMessage( Session session )
    throws MessagingException, EmailException;

  public static InternetAddress convertAddress( String address )
  {
    InternetAddress internetAddress = null;
    try {
      internetAddress = new InternetAddress(address);
    }
    catch( AddressException ae ) {
      IllegalArgumentException iae =  new IllegalArgumentException("Could not convert string '"+address+"' into an InternetAddress object.");
      iae.initCause(ae);
      throw iae;
    }
    return internetAddress;
  }
}
