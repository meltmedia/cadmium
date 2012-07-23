package com.meltmedia.cadmium.email;

import java.util.Dictionary;

import javax.mail.Session;

/**
 * A strategy for accessing the java mail session.
 * 
 * There must be a constructor that takes no arguments for it to be used
 */
public interface SessionStrategy
{
  public Session getSession()
    throws EmailException;
  
  public void configure(Dictionary<String, Object> config)
    throws EmailException;
}
