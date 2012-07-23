/**
 * 
 */
package com.meltmedia.cadmium.email;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Adds Velocity Template Processing onto the HtmlTextEmail
 * TODO Clean up this class
 * @author jkennedy
 *
 */
public class VelocityHtmlTextEmail extends HtmlTextEmail {
  private VelocityEngine engine;
  private VelocityContext context;
  
  public VelocityHtmlTextEmail() {
    super();
    
    this.engine = new VelocityEngine();
    this.context = new VelocityContext();
    
    try {
      engine.init();
    } catch (Exception e) {
      // If the engine doesn't init, we can't really do anything about it
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public VelocityContext getContext() {
    return context;
  }
  
  public Object getProperty(String key) {
    return context.get(key);
  }
  
  public void setProperty(String key, Object value) {
    context.put(key, value);
  }
  
  public void setProperties(Object map) {
    // TODO Update this method
    // This map will have been a javascript object converted in
    // Unsure how to handle this, so probably add it as a feature
    // later.
  }

  public String getText() throws EmailException {
    if (engine == null || context == null) {
      // If either the engine, or the context are null, just skip
      return super.getText();
    }
    
    StringWriter writer = new StringWriter();
    
    try {
      engine.evaluate(context, writer, "TextTemplate", super.getText());
    } catch (ParseErrorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new EmailException("Error Processing TextTemplate", e);
    } catch (MethodInvocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new EmailException("Error Processing TextTemplate", e);
    } catch (ResourceNotFoundException e) {
      throw new EmailException("Error Processing TextTemplate", e);
    } catch (IOException e) {
      throw new EmailException("Error Processing TextTemplate", e);
    }

    return writer.toString();
  }

  public String getHtml() throws EmailException {
    if (engine == null || context == null) {
      // If either the engine, or the context are null, just skip
      return super.getHtml();
    }
    
    StringWriter writer = new StringWriter();
    
    try {
      engine.evaluate(context, writer, "HtmlTemplate", super.getHtml());
    } catch (ParseErrorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new EmailException("Error Processing HtmlTemplate", e);
    } catch (MethodInvocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new EmailException("Error Processing HtmlTemplate", e);
    } catch (ResourceNotFoundException e) {
      throw new EmailException("Error Processing HtmlTemplate", e);
    } catch (IOException e) {
      throw new EmailException("Error Processing HtmlTemplate", e);
    }

    return writer.toString();
  }

}
