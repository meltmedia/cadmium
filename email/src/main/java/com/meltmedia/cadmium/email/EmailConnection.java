package com.meltmedia.cadmium.email;

/**
 * This class represents the connection to the email server.  This class is used to
 * abstract all of the complexity of the email service.
 */
public interface EmailConnection
{
  /**
   * Connects to the underlying transport.
   */
  public void connect()
    throws EmailException;

  /**
   * Sends an email through this service.  This method will throw a NotConnectedException if this
   * connection is not connected.
   */
  public void send( Email email )
    throws EmailException;

  /**
   * Closes the underlying transport.
   */
  public void close()
    throws EmailException;

  /**
   * Returns true if this email connection is connected to the undlying transport, false otherwise.
   */
  public boolean isConnected();

}
