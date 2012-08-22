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

public class EmailRuleSet {}

//import java.net.URL;
//
//import org.apache.commons.digester.Digester;
//import org.apache.commons.digester.Rule;
//import org.apache.commons.digester.RuleSetBase;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.xchain.framework.digester.SerializationRule;
//import org.xml.sax.Attributes;
//
//public class EmailRuleSet
//  extends RuleSetBase
//{
//  /** The log for this class. */
//  private static final Logger log = LoggerFactory.getLogger(EmailRuleSet.class);
//
//  /** The namespace for email elements - {@value}.*/
//  public static final String NAMESPACE_URI = "http://www.meltmedia.com/email/1.0";
//
//  /** The local name of the email-set element - {@value}. */
//  public static final String EMAIL_SET_TAG_NAME = "email-set";
//
//  /** The local name of the email element - {@value}. */
//  public static final String HTML_TEXT_EMAIL_TAG_NAME = "html-text-email";
//
//  /** The local name of the to element - {@value}. */
//  public static final String TO_TAG_NAME = "to";
//
//  /** The local name of the cc element - {@value}. */
//  public static final String CC_TAG_NAME = "cc";
//
//  /** The local name of the bcc element - {@value}. */
//  public static final String BCC_TAG_NAME = "bcc";
//
//  /** The local name of the from element - {@value}. */
//  public static final String FROM_TAG_NAME = "from";
//
//  /** The local name of the reply-to element - {@value}. */
//  public static final String REPLY_TO_TAG_NAME = "reply-to";
//
//  /** The local name of the text element - {@value}. */
//  public static final String TEXT_TAG_NAME = "text";
//
//  /** The local name of the html element - {@value}. */
//  public static final String HTML_TAG_NAME = "html";
//
//  /** The local name of the subject element - {@value}. */
//  public static final String SUBJECT_TAG_NAME = "subject";
//
//  /** The local name of the inline element - {@value}. */
//  public static final String INLINE_HTML_ELEMENT = "inline";
//
//  /** The local name of the content-id attribute - {@value}. */
//  public static final String CONTENT_ID_ATTRIBUTE = "cid";
//
//  /** The local name of the source attribute - {@value}. */
//  public static final String SOURCE_ATTRIBUTE = "src";
//  
//  /** The local name of the attachment element - {@value}. */
//  public static final String ATTACHMENT_ELEMENT = "attachment";
//
//  public static String getAttribute( Attributes attributes, String namespace, String localName, String defaultValue )
//  {
//    String value = defaultValue;
//
//    int index = attributes.getIndex(namespace, localName);
//    if( index != -1 ) {
//      value = attributes.getValue(index);
//    }
//
//    return value;
//  } 
//
//  public EmailRuleSet()
//  {
//    this.namespaceURI = NAMESPACE_URI;
//  }
//
//  public void addRuleInstances(Digester digester)
//  {
//    log.debug("Adding chain rules to digester.");
//    // set up the namespace in the digester.
//    digester.setNamespaceAware(true);
//    digester.setRuleNamespaceURI(namespaceURI);
//
//    // all of the address rules.
//    Rule setToAddressRule = new SetToAddressRule();
//    Rule setCcAddressRule = new SetCcAddressRule();
//    Rule setBccAddressRule = new SetBccAddressRule();
//    Rule setFromAddressRule = new SetFromAddressRule();
//    Rule setReplyToAddressRule = new SetReplyToAddressRule();
//
//    Rule setSubjectRule = new SetSubjectRule();
//    Rule setTextRule = new SetTextRule();
//    Rule setHtmlRule = new SetHtmlRule();
//
//    // all of the email rules.
//    Rule createHtmlTextEmailRule = new CreateHtmlTextEmailRule();
//    
//    // attachment rules.
//    Rule addAttachmentRule = new AddAttachmentRule();
//
//    //
//    // register the rules.
//    //
//
//    // add rules for email types.
//
//    // add rules for addresses.
//    addRuleForAddress( digester, TO_TAG_NAME, setToAddressRule );
//    addRuleForAddress( digester, CC_TAG_NAME, setCcAddressRule );
//    addRuleForAddress( digester, BCC_TAG_NAME, setBccAddressRule );
//    addRuleForAddress( digester, FROM_TAG_NAME, setFromAddressRule );
//    addRuleForAddress( digester, REPLY_TO_TAG_NAME, setReplyToAddressRule );
//    addRuleForAddress( digester, SUBJECT_TAG_NAME, setSubjectRule );
//
//    // add rules for setting the email content.
//    addRuleForContent( digester, TEXT_TAG_NAME, setTextRule );
//    addRuleForContent( digester, HTML_TAG_NAME, setHtmlRule );
//    addRulesForEmail( digester, HTML_TEXT_EMAIL_TAG_NAME, createHtmlTextEmailRule );
//    
//    // add rules for attachments.
//    addRuleForAttachment( digester, ATTACHMENT_ELEMENT, addAttachmentRule);
//  }
//  
//  public static void addRuleForAttachment( Digester digester, String tagName, Rule createRule )
//  {
//    digester.addRule( tagName, createRule );
//    digester.addRule( "*/"+tagName, createRule );
//  }
//
//  public static void addRulesForEmail( Digester digester, String tagName, Rule createRule )
//  {
//    digester.addRule( tagName, createRule );
//    digester.addRule( "*/"+tagName, createRule );
//  }
//
//  public static void addRuleForAddress( Digester digester, String tagName, Rule setRule )
//  {
//    digester.addRule( HTML_TEXT_EMAIL_TAG_NAME+"/"+tagName, setRule );
//    digester.addRule( "*/"+HTML_TEXT_EMAIL_TAG_NAME+"/"+tagName, setRule );
//  }
//
//  public static void addRuleForContent( Digester digester, String tagName, Rule setRule )
//  {
//    digester.addRule( HTML_TEXT_EMAIL_TAG_NAME+"/"+tagName, setRule );
//    digester.addRule( "*/"+HTML_TEXT_EMAIL_TAG_NAME+"/"+tagName, setRule );
//  }
//
//  public static class EmailConnectionRule
//    extends Rule
//  {
//    public void begin( String namespaceUri, String name, Attributes attributes )
//      throws Exception
//    {
//        EmailConnection connection = EmailService.getInstance().openConnection();
//        connection.connect();
//        getDigester().push(connection);
//    }
//
//    public void end( String namespace, String name )
//      throws Exception
//    {
//        EmailConnection connection = (EmailConnection)getDigester().pop();
//        connection.close();
//    }
//  }
//
//  public static class CreateHtmlTextEmailRule
//    extends Rule
//  {
//    protected EmailConnection connection = null;
//
//    public void begin( String namespaceUri, String name, Attributes attributes )
//    {
//      // create a new html text email.
//      HtmlTextEmail htmlTextEmail = new HtmlTextEmail();
// 
//      // push the html text email onto the stack.
//      getDigester().push(htmlTextEmail);
//    }
//
//    public void end( String namespace, String name )
//      throws Exception
//    {
//      // get the html text email.
//      HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().pop();
//
//
//        if( connection == null ) {
//          connection = EmailService.getInstance().openConnection();
//          connection.connect();
//        }
//
//      if( htmlTextEmail.getHtml() == null ) {
//        htmlTextEmail.setHtml("<html></html>");
//      }
//
//      if( htmlTextEmail.getText() == null ) {
//        htmlTextEmail.setText("text");
//      }
//
//        // send the email.
//        try {
//          connection.send(htmlTextEmail);
//        } 
//        catch( Exception e ) {
//          e.printStackTrace();
//        }
//    }
//
//    public void finish()
//    {
//      try {
//        connection.close();
//      }
//      catch( Exception e ) {
//      }
//    }
//  }
//
//  public static class SetSubjectRule
//    extends Rule
//  {
//    public void body( String namespace, String name, String text )
//      throws Exception
//    {
//      if( text != null ) {
//        HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek();
//
//        htmlTextEmail.setSubject(text);
//      }
//    }
//  }
//    
//  public static class SetToAddressRule
//    extends Rule
//  {
//    public void body( String namespace, String name, String text )
//      throws Exception
//    {
//      if( text != null ) {
//        HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek();
//
//        htmlTextEmail.getToSet().add(text);
//      }
//    }
//  }
//    
//  public static class SetCcAddressRule
//    extends Rule
//  {
//    public void body( String namespace, String name, String text )
//      throws Exception
//    {
//      if( text != null ) {
//        HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek();
//
//        htmlTextEmail.getCcSet().add(text);
//      }
//    }
//  }
//    
//  public static class SetBccAddressRule
//    extends Rule
//  {
//    public void body( String namespace, String name, String text )
//      throws Exception
//    {
//      if( text != null ) {
//        HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek();
//
//        htmlTextEmail.getBccSet().add(text);
//      }
//    }
//  }
//    
//  public static class SetFromAddressRule
//    extends Rule
//  {
//    public void body( String namespace, String name, String text )
//      throws Exception
//    {
//      if( text != null ) {
//        HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek();
//
//        htmlTextEmail.setFrom(text);
//      }
//    }
//  }
//
//  public static class SetReplyToAddressRule
//    extends Rule
//  {
//    public void body( String namespace, String name, String text )
//      throws Exception
//    {
//      if( text != null ) {
//        HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek();
//
//        htmlTextEmail.setReplyTo(text);
//      }
//    }
//  }
//
//  public static class SetTextRule
//    extends Rule
//  {
//    public void body( String namespace, String name, String text )
//      throws Exception
//    {
//      HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek();
//      if( text != null ) {
//        htmlTextEmail.setText(text);
//      }
//      else {
//        htmlTextEmail.setText("");
//      }
//    }
//  }
//
//
//  public static class SetHtmlRule
//    extends SerializationRule
//  {
//    public SetHtmlRule()
//    {
//      this.method = "html";
//      this.includeContainingElement = false;
//    }
//
//    public void begin( String namespaceUri, String name, Attributes attributes )
//      throws Exception
//    {
//      super.begin( namespaceUri, name, attributes );
//    }
//
//    public void end()
//      throws Exception
//    {
//      // get the buffer from the top of the stack.
//      StringBuffer buffer = (StringBuffer)getDigester().peek();
//
//      // get the email from the second to next top of the stack.
//      HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek(1);
//
//      // set the html.
//      htmlTextEmail.setHtml(buffer.toString());
//
//      // end the buffer.
//      super.end();
//    }
//  }
//  
//  public static class AddAttachmentRule
//    extends SerializationRule
//  {
//    
//    private boolean isUrl = false;
//    private String filename;
//    private String mimeType;
//    private String url;
//  
//    public void begin( String namespaceUri, String name, Attributes attributes )
//      throws Exception
//    {
//      final String url = attributes.getValue("url");
//      if( url != null && !"".equals(url) ) {
//        isUrl = true;
//        this.url = url;
//      }
//      filename = attributes.getValue("file-name");
//      mimeType = attributes.getValue("mime-type");
//      log.debug("Found attachment: url '{}', filename '{}', mimetype '{}'.", new Object[] {url, filename, mimeType});
//    }
//    
//    public void body( String namespace, String name, String text )
//      throws Exception
//    {
//      log.debug("Found name '{}' body: '{}'", name, text);
//      getDigester().push(text);
//    }
//    
//    public void end()
//      throws Exception
//    {
//      final String content = (String) getDigester().pop();
//      final HtmlTextEmail htmlTextEmail = (HtmlTextEmail)getDigester().peek();
//      if( isUrl ) {
//        htmlTextEmail.addAttachment(new URL(url));
//      } else {
//        log.debug("Adding content '{}' to {}.", content, htmlTextEmail);
//        htmlTextEmail.addAttachment(content, mimeType, filename);
//      }
//    }
//  }
//}
