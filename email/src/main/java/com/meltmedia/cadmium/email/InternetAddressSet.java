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

import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import java.util.HashSet;

public class InternetAddressSet
  extends HashSet<Object>
{
  private static final long serialVersionUID = -8452047226995826952L;

  /**
   * Returns true if o is an instance of InternetAddress and is in the set.  Returns true if o is an instance of String
   * and the set contains InternetAddress(o).  Returns false in all other cases, including when o is null.
   */
  public boolean contains( Object o )
  {
    boolean result = false;
    if( o == null ) {
      result = false;
    }
    else if( o instanceof String ) {
      result = super.contains(convertAddress((String)o));
    }
    else if( o instanceof InternetAddress ) {
      result = super.contains(o);;
    }
    return result;
  }

  /**
   * Adds a specified address to this set.  If o is an instance of String, then a new InternetAddress is created and that
   * object is added to the set.  If o is an instance of InternetAddress, then it is added to the set.  All other values of
   * o, including null, throw an IllegalArgumentException.
   */
  public boolean add( Object o )
  {
    boolean result = false;
    if( o == null ) {
      throw new IllegalArgumentException("Address sets do not support null.");
    }
    else if( o instanceof String ) {
      result = super.add(convertAddress((String)o));
    }
    else if( o instanceof InternetAddress ) {
      result = super.add(o);
    }
    else {
      throw new IllegalArgumentException("Address sets cannot take objects of type '"+o.getClass().getName()+"'.");
    }
    return result;
  }

  /**
   * Removes the specified address from this set.  If o is of type String, then a new InternetAddress is created for it and
   * an attempt is made to remove that address.
   */
  public boolean remove( Object o )
  {
    boolean result = false;

    if( o == null ) {
      result = false;
    }
    else if( o instanceof String ) {
      result = super.remove(convertAddress((String)o));
    }
    else if( o instanceof InternetAddress ) {
      result = super.remove(o);
    }

    return result;
  }

  public InternetAddress[] toInternetAddressArray()
  {
    return (InternetAddress[])toArray(new InternetAddress[size()]);
  }

  public static InternetAddress convertAddress( String address )
  {
    InternetAddress internetAddress = null;
    try {
      internetAddress = new InternetAddress(address);
    }
    catch( AddressException ae ) {
      IllegalArgumentException iae = new IllegalArgumentException("Could not convert string '"+address+"' into an InternetAddress object.");
      iae.initCause(ae);
      throw iae;
    }
    return internetAddress;
  }
}
