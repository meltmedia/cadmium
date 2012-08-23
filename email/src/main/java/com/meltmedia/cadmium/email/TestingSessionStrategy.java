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

import java.util.Dictionary;
import java.util.Properties;

import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A session strategy to use in test cases.  This strategy uses the following system properties to configure the session strategy:<br>
 * <table>
 *   <tr><td>javax.mail.Session property</td><td>system property</td></tr>
 *   <tr><td>mail.transport.protocol</td><td>com.meltmedia.email.test.protocol</td></tr>
 *   <tr><td>mail.&lt;protocol&gt;.host</td><td>com.meltmedia.email.test.host</td></tr>
 *   <tr><td>mail.&lt;protocol&gt;.port</td><td>com.meltmedia.email.test.port</td></tr>
 *   <tr><td>mail.&lt;protocol&gt;.user</td><td>com.meltmedia.email.test.user</td></tr>
 *   <tr><td>mail.&lt;protocol&gt;.password</td><td>com.meltmedia.email.test.password</td></tr>
 * </table>
 * <br>
 * <b>WARNING: This class should not be used in production, since it requires the email password to be passed as a system property.</b>
 */
public class TestingSessionStrategy
  implements SessionStrategy
{
  private static final Logger log = LoggerFactory.getLogger(TestingSessionStrategy.class);
  
  /** The system property for the testing protocol - {@value}. */
  public static final String PROTOCOL_SYSTEM_PROPERTY = "com.meltmedia.email.test.protocol";

  /** The system property for the testing host - {@value}. */
  public static final String HOST_SYSTEM_PROPERTY = "com.meltmedia.email.test.host";

  /** The system property for the testing port - {@value}. */
  public static final String PORT_SYSTEM_PROPERTY = "com.meltmedia.email.test.port";

  /** The system property for the testing user - {@value}. */
  public static final String USER_SYSTEM_PROPERTY = "com.meltmedia.email.test.user";

  /** The system property for the testing password - {@value}. */
  public static final String PASSWORD_SYSTEM_PROPERTY = "com.meltmedia.email.test.password";

  protected Session session = null;

  public TestingSessionStrategy()
  {
    Properties properties = null;
    try {
      String protocol = getSystemProperty(PROTOCOL_SYSTEM_PROPERTY);
      String host     = getSystemProperty(HOST_SYSTEM_PROPERTY);
      String port     = getSystemProperty(PORT_SYSTEM_PROPERTY);
      String user     = getSystemProperty(USER_SYSTEM_PROPERTY);
      String password = getSystemProperty(PASSWORD_SYSTEM_PROPERTY);
  
      properties = new Properties();
      properties.put("mail.transport.protocol", protocol);
      properties.put("mail."+protocol+".host", host);
      properties.put("mail."+protocol+".port", port);
      properties.put("mail."+protocol+".user", user);
      properties.put("mail."+protocol+".password", password);
    } catch( IllegalStateException e ) {
      log.debug("Required system properties missing. No ", Session.class.getName(), " instance created.");
      return;
    }
    session = Session.getDefaultInstance(properties, null);
  }

  public Session getSession()
    throws EmailException
  {
    if( session == null )
      throw new EmailException("No session available due to missing system properties.");
    return session;
  }

  protected String getSystemProperty( String propertyName )
  {
    String property = System.getProperty(propertyName);

    if( property == null ) {
      throw new IllegalStateException("The testing session startegy requires the system property '"+propertyName+"'.");
    }

    return property;
  }

  public void configure(Dictionary<String, Object> config)
      throws EmailException {
  }
}
