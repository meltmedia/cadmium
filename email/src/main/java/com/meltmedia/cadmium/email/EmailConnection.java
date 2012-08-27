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
