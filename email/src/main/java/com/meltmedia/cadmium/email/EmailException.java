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
