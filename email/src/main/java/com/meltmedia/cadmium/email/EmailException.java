package com.meltmedia.cadmium.email;

/**
 * This is the top level exception.
 */
public class EmailException
  extends Exception
{
  private static final long serialVersionUID = -5113186761657535719L;

  /**
   * Creates a new email exception with a message and a cause.
   */
  public EmailException( String message, Throwable cause )
  {
    super( message, cause );
  }

  /**
   * Creates a new email exception with just a message.
   */
  public EmailException( String message )
  {
    super( message );
  }

  /**
   * Creates a new email exception with just a cause.
   */
  public EmailException( Throwable cause )
  {
    super( cause );
  }
}
