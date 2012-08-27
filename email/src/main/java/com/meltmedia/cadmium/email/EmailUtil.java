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

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of static utility methods and constants to aid in the creation of javamail messages.
 */
public class EmailUtil
{
  private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);

  /** The content disposition for attachments that require user action - {@value} */
  public static final String DISPOSITION_ATTACHMENT = "attachment";
  /** The content disposition for attachments that are shown in the email client - {@value} */
  public static final String DISPOSITION_INLINE = "inline";
  /** The regex used for parsing content disposition file names from url paths - {@value}. */
  public static final String FILE_NAME_REGEX = "\\A.*?([^/])*\\Z";
  /** The pattern used for parsing content disposition file names from url paths. */
  public static Pattern FILE_NAME_PATTERN = null;

  /** The mime subtype for mixed mime messages - {@value}. */
  public static final String SUBTYPE_MIXED = "mixed";
  /** The mime subtype for alternative mime messages - {@value}. */
  public static final String SUBTYPE_ALTERNATIVE = "alternative";
  /** The mime subtype for related mime messages - {@value}. */
  public static final String SUBTYPE_RELATED = "related";

  /** The mime type for html messages - {@value}. */
  public static final String TYPE_TEXT_HTML = "text/html";
  /** The mime type for text messages - {@value}. */
  public static final String TYPE_TEXT_PLAIN  = "text/plain";

  public static final String UTF_8_CHARACTER_SET = "; charset=UTF-8";

  static {
    try {
      FILE_NAME_PATTERN = Pattern.compile("\\A.*?([^/])*\\Z");
    }
    catch( PatternSyntaxException pse ) {
      log.error("Could not compile file name pattern.");
    }
  }

  /**
   * Creates a body part for a multipart.
   */
  public static MimeBodyPart newMultipartBodyPart( Multipart multipart )
    throws MessagingException
  {
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(multipart);
    return mimeBodyPart;
  }

  public static MimeBodyPart newTextBodyPart( String text )
    throws MessagingException
  {
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(text, TYPE_TEXT_PLAIN+UTF_8_CHARACTER_SET);
    return mimeBodyPart;
  }

  public static MimeBodyPart newHtmlBodyPart( String html )
    throws MessagingException
  {
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(html, TYPE_TEXT_HTML+UTF_8_CHARACTER_SET);
    return mimeBodyPart;
  }

  /**
   * Creates a body part for an attachment that is used by an html body part.
   */
  public static MimeBodyPart newHtmlAttachmentBodyPart( URL contentUrl, String contentId )
    throws MessagingException
  {
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setDataHandler(new DataHandler(contentUrl));
    if( contentId != null ) {
      mimeBodyPart.setHeader("Content-ID", contentId);
    }
    return mimeBodyPart;
  }

  /**
   * Creates a body part for an attachment that is downloaded by the user.
   */
  public static MimeBodyPart newAttachmentBodyPart( URL contentUrl, String contentId )
    throws MessagingException
  {
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setDataHandler(new DataHandler(contentUrl));
    if( contentId != null ) {
      mimeBodyPart.setHeader("Content-ID", contentId);
    }
    mimeBodyPart.setDisposition(DISPOSITION_ATTACHMENT);
    String fileName = fileNameForUrl(contentUrl);
    if( fileName != null ) {
      mimeBodyPart.setFileName(fileName);
    }
    return mimeBodyPart;
  }
  
  /**
   * Creates a body part for an attachment that is downloaded by the user.
   * @throws IOException 
   */
  public static MimeBodyPart newAttachmentBodyPart( String content, String contentId, String mimeType, String fileName)
    throws MessagingException, IOException
  {
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
//    log.debug("Creating an attachment for content '{}', mimeType '{}', fileName '{}'.", new Object[] {content, mimeType, fileName});
    mimeBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(content, mimeType + UTF_8_CHARACTER_SET)));
    if( contentId != null ) {
      mimeBodyPart.setHeader("Content-ID", contentId);
    }
    mimeBodyPart.setDisposition(DISPOSITION_ATTACHMENT);
    if( fileName != null ) {
      mimeBodyPart.setFileName(fileName);
    }
    return mimeBodyPart;
  }

  /**
   * Returns the content disposition file name for a url.  If a file name cannot be parsed from this url, then null
   * is returned.
   */
  public static String fileNameForUrl( URL contentUrl )
  {
    String fileName = null;
    Matcher matcher = FILE_NAME_PATTERN.matcher(contentUrl.getPath());
    if( matcher.find() ) {
      fileName = matcher.group(1);
    }
    return fileName;
  }
}
