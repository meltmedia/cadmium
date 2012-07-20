package com.meltmedia.cadmium.email;

/**
 * 
 * @author jkennedy
 * TODO Probably need to implement BundleListner to see if we get uninstalled
 * so we can clean up
 */
public interface EmailService
{
  public void send(Email email) throws EmailException;
  public EmailConnection openConnection() throws EmailException;
}
