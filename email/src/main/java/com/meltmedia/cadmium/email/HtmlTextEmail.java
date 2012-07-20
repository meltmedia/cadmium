package com.meltmedia.cadmium.email;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class HtmlTextEmail
  extends Email
{
  protected String text = "";
  protected String html = "";
  // TODO: FIX ATTACHMENTS
  protected Set<URL> attachmentUrlSet;
  protected Set<StringAttachment> attachmentStrings;
  
  public HtmlTextEmail() {
    super();
    attachmentUrlSet = new HashSet<URL>();
    attachmentStrings = new HashSet<StringAttachment>();
  }

  public void setText( String text )
  {
    this.text = text;
  }

  public String getText() throws EmailException
  {
    return this.text;
  }

  public void setHtml( String html )
  {
    this.html = html;
  }

  public String getHtml() throws EmailException
  {
    return this.html;
  }

  public void addAttachment( URL attachmentUrl )
  {
    attachmentUrlSet.add(attachmentUrl);
  }
  
  public void addAttachment( String content, String mimeType, String filename)
  {
    attachmentStrings.add(new StringAttachment(content, mimeType, filename));
  }

  /*
  public void addHtmlRelatedAttachment( URL attachmentUrl )
  {
    htmlRelatedAttachmentUrlSet.add(attachmentUrl);
  }
  */

  public void simplify()
  {
    // have the super class simplify.
    super.simplify();
  }

  /**
   * Creates a mime message for an html emails with the following structure:
   * mime/mixed
   *  - mime/alternative
   *    - text/plain
   *    - mime/related
   *      - text/html
   *      - image/*
   *      - image/*
   *  - (attachment mime type)
   *  - (attachment mime type)
   */
  public MimeMessage newMessage( Session session )
    throws MessagingException, EmailException
  {
    MimeMessage message = new MimeMessage( session );

    // populate the addresses and subject.
    populate(message);

    // create the multipart content.
    MimeMultipart containingMultipart = new MimeMultipart(EmailUtil.SUBTYPE_MIXED);

    // create the message multipart content.
    MimeMultipart messageMultipart = new MimeMultipart(EmailUtil.SUBTYPE_ALTERNATIVE);
    containingMultipart.addBodyPart(EmailUtil.newMultipartBodyPart(messageMultipart));

    // create the text part.
    messageMultipart.addBodyPart(EmailUtil.newTextBodyPart(getText()));

    // create the html part.
    MimeMultipart htmlMultipart = new MimeMultipart(EmailUtil.SUBTYPE_RELATED);
    htmlMultipart.addBodyPart(EmailUtil.newHtmlBodyPart(getHtml()));
    messageMultipart.addBodyPart(EmailUtil.newMultipartBodyPart(htmlMultipart));

    /*
    // iterate over the html context, creating new elements for each.
    Iterator htmlAttachmentIterator = htmlAttachmentMap.entrySet().iterator();
    while( htmlAttachmentIterator.hasNext() ) {
      Map.Entry htmlAttachmentEntry = (Map.Entry)htmlAttachmentIterator.next();
      htmlMultipart.addBodyPart(EmailUtil.newHtmlAttachmentPart((URL)htmlAttachmentEntry.getKey(), (String)htmlAttachmentPart.getValue());
    }

    // add the html multipart to the message multipart.
    messageMultipart.addBodyPart(EmailUtil.newMultipartBodyPart(htmlMultipartBodyPart));
    */
  
      
    // create the attachments.
    for( URL attachmentUrl : attachmentUrlSet ) {
      containingMultipart.addBodyPart(EmailUtil.newAttachmentBodyPart(attachmentUrl, null));
    }
    try {
      for( StringAttachment attachment : attachmentStrings ) {
        containingMultipart.addBodyPart(EmailUtil.newAttachmentBodyPart(attachment.getContent(), null, attachment.getMimeType(), attachment.getFilename()));
      }
    } catch (IOException ioe) {
      throw new MessagingException("Unable to create attachment for string.", ioe);
    }

    message.setContent(containingMultipart);

    // save the changes.
    message.saveChanges();

    // return the email.
    return message;
  }
  
  public static class StringAttachment {
    private String content;
    private String mimeType;
    private String filename;
    
    public StringAttachment() {
      this(null, null, null);
    }
    
    public StringAttachment(String content, String mimeType, String filename) {
      super();
      setContent(content);
      setMimeType(mimeType);
      setFilename(filename);
    }
    
    public void setContent(String content) {
      this.content = content;
    }
    
    public String getContent() {
      return content;
    }
    
    public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
    }
    
    public String getMimeType() {
      return mimeType;
    }
    
    public void setFilename(String filename) {
      this.filename = filename;
    }
    
    public String getFilename() {
      return filename;
    }
  }
}
